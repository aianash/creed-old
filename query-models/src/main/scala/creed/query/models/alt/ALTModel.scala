package creed
package query
package models
package alt

import scala.collection.JavaConversions._
import scala.collection.immutable.SortedSet
import scala.collection.mutable.{Seq => MSeq, SortedSet => MSortedSet}

import java.util.{NavigableSet => JNavigableSet, Set => JSet, SortedSet => JSorteSet}
import java.util.concurrent.ConcurrentHashMap
import java.io.File

import it.unimi.dsi.fastutil.objects.{Object2DoubleOpenHashMap, Object2IntOpenHashMap, Object2ObjectOpenHashMap, ObjectRBTreeSet, ObjectArrayList, Object2ObjectRBTreeMap}

import core._

import org.mapdb._
import datasets._

import hemingway.dictionary._, similarity.Jaccard


/** ALT model contains the simsets for activities/looks/timeWeathers
  * primary use is to get simsets for a given
  * @param db - Trained model file
  */
class ALTModel(db: DB) {

  import SimSets.Namespace

  private val _simsets = new SimSets(db)
  private val intents = FileBasedDictionary("intents_dict", db)

  private val intentSim = Jaccard(0.4)

  def simsets(nouns: Seq[String], max: Int = 10): (ScoredSimSetIds[Activity], ScoredSimSetIds[Look], ScoredSimSetIds[TimeWeather]) = {
    val intents = nouns.flatMap(this.intents.findSimilar(_, intentSim))
                       .map(entry => entry.str.get -> entry.payload("type"))
                       .groupBy(_._2)

    val activities   = intents.get("activity").map(_.map(_._1)) getOrElse Seq.empty[String]
    val looks        = intents.get("look").map(_.map(_._1)) getOrElse Seq.empty[String]
    val timeWeathers = intents.get("timeWeather").map(_.map(_._1)) getOrElse Seq.empty[String]

    val activitySimsets    = simsets[Activity](activities, looks, timeWeathers)
    val lookSimsets        = simsets[Look](looks, activities, timeWeathers)
    val timeWeatherSimsets = simsets[TimeWeather](timeWeathers, activities, looks)

    (activitySimsets, lookSimsets, timeWeatherSimsets)
  }

  private def simsets[I <: Intent : Namespace](intents: Seq[String], context_1: Seq[String], context_2: Seq[String]): ScoredSimSetIds[I] =
    intents.foldLeft(ScoredSimSetIds[I](IndexedSeq.empty)) { (result, intent) =>
      result ++ _simsets.simsetsAnyContext[I](intent, context_1 -> context_2)
    }

}

object ALTModel {

  import SimSets.Namespace

  def apply(modelFilePath: String) = new ALTModel(makeDB(modelFilePath))

  private def init(config: Map[String, String]) = {
    val dataset = IntentDataset(config("query.alt.dataset"))
    val modelFile = new File(config("query.alt.model-file"))
    if(modelFile.exists() || modelFile.isDirectory)
      throw new IllegalArgumentException("File at path should not already exist and shouldn't be a directory")
    (dataset, makeDB(modelFile))
  }

  def train(config: Map[String, String]) {
    val (dataset, db) = init(config)

    val intentDict = FileBasedDictionary("intents_dict", db)

    val altRelNumer = new Object2DoubleOpenHashMap[ALT]
    val altRelDenom = new Object2IntOpenHashMap[ALT]
    altRelNumer.defaultReturnValue(0.0)
    altRelDenom.defaultReturnValue(0)

    for(_ @ (alt, rel, count) <- dataset.altIterator) {
      val numer = altRelNumer.get(alt) + rel * count
      val denom = altRelDenom.get(alt) + count
      altRelNumer.put(alt, numer)
      altRelDenom.put(alt, denom)
    }

    val actOccurences = new Occurrences
    val lkOccurences  = new Occurrences
    val twOccurences  = new Occurrences

    for(entry <- altRelNumer.object2DoubleEntrySet.fastIterator) {
      val alt   = entry.getKey
      val numer = entry.getDoubleValue
      val denom = altRelDenom.get(alt)
      val p     = numer / denom // relevance of the alt
                                // or how much this combination
                                // of activity, look and timeWeather
                                // makes sense.

      import alt._

      actOccurences add(activity,    look     -> timeWeather, p)
      lkOccurences  add(look,        activity -> timeWeather, p)
      twOccurences  add(timeWeather, activity -> look,        p)
    }

    val activitySimsetsBuildr = SimSets.newBuildr[Activity](0.8)
    actOccurences.cooccurs.foldLeft(0.5) { (atleastIG, cooccur) => activitySimsetsBuildr.add(cooccur, atleastIG) }
    actOccurences.singles.foreach { single => activitySimsetsBuildr.add(single) }

    val lookSimsetsBuildr = SimSets.newBuildr[Look](0.8)
    lkOccurences.cooccurs.foldLeft(0.5) { (atleastIG, cooccur) => lookSimsetsBuildr.add(cooccur, atleastIG) }
    lkOccurences.singles.foreach { single => lookSimsetsBuildr.add(single) }

    val timeWeatherSimsetsBuildr = SimSets.newBuildr[TimeWeather](0.8)
    twOccurences.cooccurs.foldLeft(0.5) { (atleastIG, cooccur) => timeWeatherSimsetsBuildr.add(cooccur, atleastIG) }
    twOccurences.singles.foreach { single => timeWeatherSimsetsBuildr.add(single) }

    activitySimsetsBuildr    saveTo db
    lookSimsetsBuildr        saveTo db
    timeWeatherSimsetsBuildr saveTo db

    // add dictionary for activites, looks and timeWeather
  }

}