package creed.indexer

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version

import scala.Enumeration

object ClothingIndexFields extends Enumeration {

  protected case class IndexField(i: Int, name: String, analyzer: Option[Analyzer]) extends Val(i, name)
  def IndexField(name: String, analyzer: Option[Analyzer] = None): IndexField = IndexField(nextId, name, analyzer)

  val StoreId        = IndexField("storeId")
  val ItemId         = IndexField("itemId")
  val ItemType       = IndexField("itemType")
  val ItemTypeGroups = IndexField("itemTypeGroups")
  val NamedType      = IndexField("namedType")
  val ProductTitle   = IndexField("productTitle", Some(new StandardAnalyzer(Version.LUCENE_48)))
  val Size           = IndexField("size")
  val Brand          = IndexField("brand")
  val Description    = IndexField("description", Some(new StandardAnalyzer(Version.LUCENE_48)))
  val Price          = IndexField("price")
  val Fabric         = IndexField("fabric", Some(new StandardAnalyzer(Version.LUCENE_48)))
  val Fit            = IndexField("fit", Some(new StandardAnalyzer(Version.LUCENE_48)))
  val Style          = IndexField("style", Some(new StandardAnalyzer(Version.LUCENE_48)))
  val Color          = IndexField("color")

}