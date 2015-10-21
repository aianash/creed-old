package creed
package query
package models
package trainers

import scala.collection.JavaConversions._

import java.io.File

import scalaz._, Scalaz._

import org.mapdb._

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap}

import datasets.IntentDataset


object QueryALTModelTrainer extends ModelTrainer {

  private def init(config: TrainConfig): (IntentDataset, DB) = {
    val dataset = IntentDataset(config.get("query.alt.dataset"))
    val modelFile = new File(config.get("query.alt.model-file"))
    if(modelFile.exists() || modelFile.isDirectory)
      throw new IllegalArgumentException("File at path should not already exist and shouldn't be a directory")
    dataset -> makeDB(modelFile)
  }

  /** Train ALT model
    * - Assigning relevance
    * - expanding ALT set by finding similar ALTs and grouping similar Intents
    */
  def train(config: TrainConfig) {
    val (dataset, modelDB) = init(config)

    val bfAL  = BloomFilter[(Intent[Activity], Intent[Look])]()
    val bfAT  = BloomFilter[(Intent[Activity], Intent[TimeWeather])]()
    val bfLT  = BloomFilter[(Intent[Look], Intent[TimeWeather])]()
    val bfALT = BloomFilter[(Intent[Activity], Intent[Look], Intent[TimeWeather])]()

    val altRelNumer = new Object2FloatOpenHashMap[ALT]
    val altRelDenom = new Object2IntOpenHashMap[ALT]
    altRelNumer.defaultReturnValue(0.0F)
    altRelDenom.defaultReturnValue(0)

    for(_ @ (alt, rel, count) <- dataset.altIterator) {
      alt match {
        case ALT(activity, look, Anything)        => bfAL += activity -> look
        case ALT(Anything, look, timeWeather)     => bfLT += look -> timeWeather
        case ALT(activity, Anything, timeWeather) => bfAT += activity -> timeWeather
        case ALT(activity, look, timeWeather) =>
          bfAL  += activity -> look
          bfAT  += activity -> timeWeather
          bfLT  += look -> timeWeather
          bfALT += (activity, look, timeWeather)
      }

      val numer = altRelNumer.get(alt) + rel * count
      val denom = altRelDenom.get(alt) + count
    }

    val numActivities = dataset.count(classOf[Activity])
    val numLooks = dataset.count(classOf[Look])
    val numTimeWeathers = dataset.count(classOf[TimeWeather])

    val Pal  = Probability2[Intent[Activity], Intent[Look]](0.2f, numActivities, numLooks)
    val Plt  = Probability2[Intent[Look], Intent[TimeWeather]](0.2f, numLooks, numTimeWeathers)
    val Pat  = Probability2[Intent[Activity], Intent[TimeWeather]](0.2f, numActivities, numTimeWeathers)
    val Palt = Probability3[Intent[Activity], Intent[Look], Intent[TimeWeather]](0.3f, numActivities, numLooks, numTimeWeathers, Pal.some, Pat.some, Plt.some)

    for(entry <- altRelNumer.object2FloatEntrySet.fastIterator) {
      val alt   = entry.getKey
      val numer = entry.getFloatValue
      val denom = altRelDenom.get(alt)
      val p     = numer / denom

      import alt._
      alt match {
        case ALT(activity, look, Anything)        => Pal  += (activity, look) -> p
        case ALT(Anything, look, timeWeather)     => Plt  += (look, timeWeather) -> p
        case ALT(activity, Anything, timeWeather) => Pat  += (activity, timeWeather) -> p
        case ALT(activity, look, timeWeather)     => Palt += (activity, look, timeWeather) -> p
      }
    }

    val ssActivities  = SimSets[Intent[Activity]]()
    val ssLooks       = SimSets[Intent[Look]]()
    val ssTimeWeather = SimSets[Intent[TimeWeather]]()

  }

}