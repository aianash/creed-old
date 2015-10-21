package creed
package query
package models

import scala.collection.JavaConversions._

import java.io.File

import scalaz._, Scalaz._

import org.mapdb._

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap}

import client.search.Query
import datasets.IntentDataset


class QueryALTModel(db: DB) {

  def alt(query: Query): Option[ALT] = {
    ALT(Activity("party"), Look("modern"), TimeWeather("night")).some
  }

  def alts: Iterator[ALT] = Iterator.empty

}

object QueryALTModel {
  def apply(modelFilePath: String) = new QueryALTModel(makeDB(modelFilePath))
}


case class BloomFilter[T]() {
  def +=(e: T) = this
}