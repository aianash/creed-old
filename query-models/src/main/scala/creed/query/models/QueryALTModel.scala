package creed
package query
package models

import java.io.File

import scalaz._, Scalaz._

import org.mapdb._

import client.search.Query
import datasets.IntentDataset


class QueryALTModel(db: DB) {

  def alt(query: Query): Option[ALT] = {
    // dataset.findSimilar(clean(query.queryStr))
    ALT(Activity("party"), Look("modern"), TimeWeather("night")).some

  }

}

object QueryALTModel {
  def apply(modelFilePath: String) = new QueryALTModel(mkDB(modelFilePath))

  private[models] def mkDB(dbFile: File): DB =
    DBMaker.fileDB(dbFile)
           .asyncWriteFlushDelay(1)
           .cacheHardRefEnable
           .transactionDisable
           .closeOnJvmShutdown
           .compressionEnable
           .fileMmapEnableIfSupported
           .make

  private[models] def mkDB(dbPath: String): DB = mkDB(new File(dbPath))
}