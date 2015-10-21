package creed
package query
package models
package trainers

trait ModelTrainer {
  def train(config: TrainConfig): Unit
}

class TrainConfig extends java.util.HashMap[String, String]
