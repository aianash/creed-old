package creed
package query
package datasets

import org.mapdb._

import core._, utils.MapDBUtils

import commons.catalogue._, attributes._

// [TO DO] In next stage add brand, sizes, etc
case class ItemFeature(
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
  def +=(entry: (ALT, ItemFeature, Float)): Unit = add(entry)
  def add(entry: (ALT, ItemFeature, Float)): Unit = {}
  def iterator: Iterator[(ALT, ItemFeature, Float, Int)] = Iterator.empty
  def iterator(alt: ALT): Iterator[(ItemFeature, Float, Int)] = Iterator.empty
}