package creed
package query
package models

import client.search.Query

import scalaz._, Scalaz._

import datasets.IntentDataset

import org.mapdb._

class QueryALTModel { ///(db: DB) {

  def alt(query: Query): Option[ALT] = {
    // dataset.findSimilar(clean(query.queryStr))
    ALT(Activity("party"), Look("modern"), TimeWeather("night")).some

  }

}

// object QueryALTModel {
//   def apply(modelFilePath: String) = new QueryALTModel(mkDB(datasetFilePath))

//   private def mkDB(dbFile: File) =
//     DBMaker.fileDB(dbFile)
//           .asyncWriteFlushDelay(1)
//           .cacheHardRefEnable
//           .transactionDisable
//           .closeOnJvmShutdown
//           .compressionEnable
//           .fileMmapEnableIfSupported
//           .make

//   private def mkDB(dbPath: String) = mkDB(new File(dbPath))

//   def train(dataset: IntentDataset, modelFilePath: String) {
//     val file = new File(modelFilePath)

//     val modelDB = mkDB(file)



  // }

// }


// object QueryALTModelTrainer extends ModelTrainer {

//   private def init(config) = {
//     val dataset = IntentDataset(config("query.alt.dataset"))
//     val modelFile = new File(config("query.alt.model-file"))
//     if(file.exists() || file.isDirectory)
//       throw new IllegalArgumentException("File at path should not already exist and shouldn't be a directory")
//     dataset -> QueryALTModel.mkDB(modelFile)
//   }

//   def train(config: TrainConfig) {
//     val (dataset, modelDB) = init(config)
//     val alts = dataset.altIterator
//     for(alt <- alts) {

//     }
//   }

// }


// // SHIFT

// trait ModelTrainer {
//   def train(config: TrainConfig)
// }

// class TrainConfig extends Map[String, String]
