package creed
package query
package models

import scala.collection.JavaConversions._
import scala.collection.immutable.SortedSet
import scala.reflect.ClassTag

import java.util.{NavigableSet => JNavigableSet, Set => JSet, SortedSet => JSorteSet}
import java.util.concurrent.ConcurrentHashMap
import java.io.File

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap, ObjectRBTreeSet}

import core._

import org.mapdb._
import datasets._


/** This ALT model is used to find similar intents to a given intent
  * This models is trained using intent dataset
  *
  * @param db - Trained model file
  */
class ALTModel(db: DB) {

  private val activityCooccurs    = db.treeSetCreate("_activities_cooccurences") .serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]
  private val lookCooccurs        = db.treeSetCreate("_looks_cooccurences")      .serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]
  private val timeWeatherCooccurs = db.treeSetCreate("_timeweather_cooccurences").serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]

  private val simsets = ALTSimSets.load(db)

  def alts: Iterator[ALT] = Iterator.empty

  def similary[I <: Intent[I]](intent: I)(implicit buildr: IntentBuildr[I]): SortedSet[(I, Float)] = intent match {
    case Activity(activity)       => findSimilar(activityCooccurs,    activity,    buildr.build(_))
    case Look(look)               => findSimilar(lookCooccurs,        look,        buildr.build(_))
    case TimeWeather(timeWeather) => findSimilar(timeWeatherCooccurs, timeWeather, buildr.build(_))
  }

  private def findSimilar[I <: Intent[I]](in: JNavigableSet[Array[Object]], target: String, instantiate: String => I): SortedSet[(I, Float)] = {
    implicit val ordering = Ordering.by[(I, Float), (Float, String)](ir => ir._2 -> ir._1.value)
    Fun.filter(in, target).foldLeft(SortedSet.newBuilder[(I, Float)]) { (builder, arr) =>
      val res = instantiate(arr(1).asInstanceOf[String])
      builder += res -> arr(2).asInstanceOf[Float]
    } result()
  }

}

object ALTModel {
  def apply(modelFilePath: String) = new ALTModel(makeDB(modelFilePath))
}