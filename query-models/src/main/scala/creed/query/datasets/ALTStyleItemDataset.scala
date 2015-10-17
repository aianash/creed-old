package creed
package query
package datasets

import org.mapdb._

import core._, utils.MapDBUtils

import commons.catalogue._, attributes._

// [TO DO] In next stage add brand, sizes, etc
case class DatasetItemFeature(
  itemTypeGroup: ItemTypeGroup,
  styles: Seq[ClothingStyle],
  fabric: ApparelFabric,
  fit: ApparelFit,
  colors: Colors,
  stylingTips: StylingTips,
  descr: Description)

/** Description of function
  *
  * @param Parameter1 - blah blah
  * @return Return value - blah blah
  */
class ALTItemRelevanceDataset(db: DB) {
  def +=(entry: (ALT, DatasetItemFeature, Float)): Unit = add(entry)
  def add(entry: (ALT, DatasetItemFeature, Float)): Unit = {}
  def iterator: Iterator[(ALT, DatasetItemFeature, Float, Int)] = Iterator.empty
  def iterator(alt: ALT): Iterator[(DatasetItemFeature, Float, Int)] = Iterator.empty
}

object ALTItemRelevanceDataset {

  def apply(filePath: String): ALTItemRelevanceDataset = apply(makeDB(filePath))
  def apply(db: DB) = new ALTItemRelevanceDataset(db)

}