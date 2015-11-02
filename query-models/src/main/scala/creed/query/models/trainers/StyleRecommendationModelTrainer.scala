package creed
package query
package models
package trainers

import scala.collection.mutable.Map

import commons.catalogue._, attributes._

import creed.query.datasets._
import creed.query.ALTStyleSet


object StyleRecommendationModelTrainer extends ModelTrainer {

  private val scoreMap = Map.empty[(ALT, ClothingStyle), Float]
  private val countMap = Map.empty[(ALT, ClothingStyle), Int]

  private def init(config: TrainConfig) = {
    val dataset = ALTItemRelevanceDataset(config.get("query.alt-style-relevance.dataset"))
    val trainer = ALTStyleSet.trainer(config.get("query.alt-style-relevance.path"))

    (dataset, trainer)
  }

  def train(config: TrainConfig): Unit = {
    val (dataset, trainer) = init(config)
    processDataset(dataset.iterator[ClothingStyle])
    scoreMap foreach { case (k, v) =>
      val avgScore = v / countMap.get(k).get
      trainer.add(k._1, k._2, avgScore)
    }
    trainer.done
  }

  private def processDataset(iterator: Iterator[(ALT, ClothingStyle, Float, Int)]): Unit = {
    iterator foreach { elem =>
      var score = elem._3 * elem._4
      var count = elem._4
      if(scoreMap.contains((elem._1, elem._2))) {
        score += scoreMap.get((elem._1, elem._2)).get
        count += countMap.get((elem._1, elem._2)).get
        scoreMap -= ((elem._1, elem._2))
        countMap -= ((elem._1, elem._2))
      }
      scoreMap += ((elem._1, elem._2) -> score)
      countMap += ((elem._1, elem._2) -> count)
    }
  }

}