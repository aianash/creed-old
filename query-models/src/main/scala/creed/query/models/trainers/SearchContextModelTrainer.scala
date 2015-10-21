package creed
package query
package models
package trainers

import scala.collection.JavaConversions._

import java.io.File

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap}

import commons.catalogue.attributes._

import datasets.ALTItemRelevanceDataset


object SearchContextModelTrainer extends ModelTrainer {

  def init(config: TrainConfig) = {
    val dataset = ALTItemRelevanceDataset(config.get("query.style-item.dataset"))
    val modelFile = new File(config.get("query.search-context.model-file"))
    val altModel = ALTModel(config.get("query.alt.model-file"))
    if(modelFile.exists() || modelFile.isDirectory)
      throw new IllegalArgumentException("File at path should not already exist and shouldn't be a directory")
    (dataset, makeDB(modelFile), altModel)
  }


  // 1. for each type of feature (alt, clothingStyle) -> feature mapping with relevance
  def train(config: TrainConfig) {
    val (dataset, modelDB, altModel) = init(config)

    val relNumer = new Object2FloatOpenHashMap[(ALT, ClothingStyle, Trait)]
    val relDenom = new Object2FloatOpenHashMap[(ALT, ClothingStyle, Trait)]
    relNumer.defaultReturnValue(0.0F)
    relDenom.defaultReturnValue(0)

    for(_ @ (alt, feature, relevance, count) <- dataset.iterator) {
      import feature._
      import TraitBuildrs._
      val traits =
        Traits(fabric) ++
        Traits(fit) ++
        Traits(colors) ++
        Traits(stylingTips) ++
        Traits(descr)

      for(style <- styles; itemTrait <- traits) {
        val key = (alt, style, itemTrait)
        val numer = relNumer.get(key) + relevance * count
        val denom = relDenom.get(key) + count
        relNumer.put(key, numer)
        relDenom.put(key, denom)
      }

      // val (alt, feature, relevance, count) = entry
      // val numer = entry.getFloatValue
      // val denom = relDenom.get(entry.getKey)
      // val p = numer / denom

    }
  }

}