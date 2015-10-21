package creed
package query
package models
package trainers

import scala.collection.JavaConversions._

import java.io.File
import java.util.{NavigableSet => JNavigableSet}

import org.mapdb._

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap, ObjectRBTreeSet}

import datasets.IntentDataset


object ALTModelTrainer extends ModelTrainer {

  import ALTSimSets._

  def init(config: TrainConfig) = {
    val dataset = IntentDataset(config.get("query.alt.dataset"))
    val modelFile = new File(config.get("query.alt.model-file"))
    if(modelFile.exists() || modelFile.isDirectory)
      throw new IllegalArgumentException("File at path should not already exist and shouldn't be a directory")
    (dataset, makeDB(modelFile))
  }

  def train(config: TrainConfig) {
    val (dataset, model) = init(config)

    val altRelNumer = new Object2FloatOpenHashMap[ALT]
    val altRelDenom = new Object2IntOpenHashMap[ALT]
    altRelNumer.defaultReturnValue(0.0F)
    altRelDenom.defaultReturnValue(0)

    for(_ @ (alt, rel, count) <- dataset.altIterator) {
      val numer = altRelNumer.get(alt) + rel * count
      val denom = altRelDenom.get(alt) + count
      altRelNumer.put(alt, numer)
      altRelDenom.put(alt, denom)
    }

    val simsets = ALTSimSets.empty

    for(entry <- altRelNumer.object2FloatEntrySet.fastIterator) {
      val alt   = entry.getKey
      val numer = entry.getFloatValue
      val denom = altRelDenom.get(alt)
      val p     = numer / denom
      simsets   += alt -> p
    }

    simsets.save(model)
  }

}