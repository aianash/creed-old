package creed
package query
package datasets

import scala.reflect.ClassTag
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io.Source
import scala.math.Ordered

import org.mapdb._

import play.api.libs.json._

import core._, utils.MapDBUtils

import commons.catalogue._, attributes._


/**
 * This class represents a data point for catalogue items
 * [TODO] Add brand, sizes etc in later stages
 */
case class ItemFeature(
  itemTypeGroup: ItemTypeGroup,
  styles: ClothingStyles,
  fabric: ApparelFabric,
  fit: ApparelFit,
  colors: Colors,
  stylingTips: StylingTips,
  descr: Description) extends Ordered[ItemFeature] {

  def compare(that: ItemFeature) = {
    if((this.itemTypeGroup.name == that.itemTypeGroup.name) &&
      (this.styles.styles.map(_.name) == that.styles.styles.map(_.name)) &&
      (this.fabric.fabric == that.fabric.fabric) &&
      (this.fit.fit == that.fit.fit) &&
      (this.colors.values == that.colors.values) &&
      (this.descr.text == that.descr.text) &&
      (this.stylingTips.text == that.stylingTips.text)) 0 else 1
  }

  override def toString = {
    var str = s"Styles: ${styles.styles.map(_.name)}\n"
    str += s"Fabric: ${fabric.fabric}\n"
    str += s"Fit: ${fit.fit}\n"
    str += s"Colors: ${colors.values}\n"
    str += s"Description: ${descr.text}\n"
    str += s"Styling Tips: ${stylingTips.text}\n"
    str
  }

}

/**
 * This dataset represents the relevance of an ALT to an ItemFeature. It also
 * represents the relevance of an ALT to a ItemTypeGroup and a ClothingStyle
 *
 * @param {DB}     db
 * @param {String} filepath of catalogue item json file
 */
class ALTItemRelevanceDataset(db: DB) {

  import ALTItemRelevanceDataset._

  private val altsItemFeature = db.treeSetCreate("_alts_item_feature")
                                  .serializer(MapDBUtils.ARRAY6)
                                  .makeOrGet[Array[Object]]

  private val altsItemTypeGroup = db.treeSetCreate("_alts_item_type_group")
                                    .serializer(MapDBUtils.ARRAY6)
                                    .makeOrGet[Array[Object]]

  private val altsClothingStyle = db.treeSetCreate("_alts_clothing_styles")
                                    .serializer(MapDBUtils.ARRAY6)
                                    .makeOrGet[Array[Object]]

  /**
   * Alias of {add} function
   */
  def +=(entry: (ALT, ItemFeature, Float)): Unit = add(entry)

  /**
   * This function is used to add entries to the dataset
   * @param {(ALT, ItemFeature, Float)} Tuple consiting of ALT, ItemFeature and Relevance
   */
  def add(input: (ALT, ItemFeature, Float)): Unit = {
    val (alts, feature, relevance) = input
    addItemFeature(alts, feature, relevance)
    addClothingStyles(alts, feature.styles, relevance)
    addItemTypeGroup(alts, feature.itemTypeGroup, relevance)
  }

  /**
   * Iterator for given type T. T can be one of the following: {ItemFeature},
   * {ClothingStyle} and {ItemTypeGroup}
   *
   * @param T Type parameter
   */
  def iterator[T: ClassTag]: Iterator[(ALT, T, Float, Int)] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]] match {
      case ITEMFEATURECLAZZ =>
        altsItemFeature.iterator.map { arr =>
          val (alt: ALT, rel: Float, count: Int) = extract(arr, true)
          val feature = arr(3).asInstanceOf[T]
          (alt, feature, rel, count)
        }

      case CLOTHINGSTYLECLAZZ =>
        altsClothingStyle.iterator.map { arr =>
          val (alt: ALT, rel: Float, count: Int) = extract(arr, true)
          val style = ClothingStyle(arr(3).asInstanceOf[String]).asInstanceOf[T]
          (alt, style, rel, count)
        }

      case ITEMTYPEGROUPCLAZZ =>
        altsItemTypeGroup.iterator.map { arr =>
          val (alt: ALT, rel: Float, count: Int) = extract(arr, true)
          val itg = ItemTypeGroup(arr(3).asInstanceOf[String]).asInstanceOf[T]
          (alt, itg, rel, count)
        }
    }

  /**
   * Iteartor for a given type T for a given {ALT}. T can be one of the following:
   * {ItemFeature}, {ClothingStyle} and {ItemTypeGroup}
   *
   * @param T Type paramemeter
   */
  def iterator[T: ClassTag](alt: ALT): Iterator[(T, Float, Int)] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]] match {
      case ITEMFEATURECLAZZ =>
        Fun.filter(altsItemFeature, alt.activity.value, alt.look.value, alt.timeWeather.value).iterator.map { arr =>
          val (rel: Float, count: Int) = extract(arr, false)
          val feature = arr(3).asInstanceOf[T]
          (feature, rel, count)
        }

      case CLOTHINGSTYLECLAZZ =>
        Fun.filter(altsClothingStyle, alt.activity.value, alt.look.value, alt.timeWeather.value).iterator.map { arr =>
          val (rel: Float, count: Int) = extract(arr, false)
          val style = ClothingStyle(arr(3).asInstanceOf[String]).asInstanceOf[T]
          (style, rel, count)
        }

      case ITEMTYPEGROUPCLAZZ =>
        Fun.filter(altsItemTypeGroup, alt.activity.value, alt.look.value, alt.timeWeather.value).iterator.map { arr =>
          val (rel: Float, count: Int) = extract(arr, false)
          val itg = ItemTypeGroup(arr(3).asInstanceOf[String]).asInstanceOf[T]
          (itg, rel, count)
        }
    }

  /**
   * This function adds given {ALT}, {ItemFeature} and corresponding relevance to the item feature
   *
   * @param {ALT} alt
   * @param {ItemFeature} item feature
   * @param {Float} relevance of alt for given item feature
   */
  private def addItemFeature(alt: ALT, feature: ItemFeature, relevance: Float) = {
    val rel: java.lang.Float = relevance
    val count: java.lang.Integer = 1

    val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, feature, rel, count)
    val iter  = Fun.filter(altsItemFeature, alt.activity.value, alt.look.value, alt.timeWeather.value, feature, rel).iterator
    if(iter.hasNext) {
      val existing = iter.next
      altsItemFeature.remove(existing)
      val existingCount = existing(5).asInstanceOf[java.lang.Integer]
      val newCount: java.lang.Integer = existingCount.intValue() + 1
      entry(5) = newCount
    }
    altsItemFeature.add(entry)
  }

  /**
   * This function adds given {ALT}, {ClothingStyle} and corresponding relevance to the clothing style
   *
   * @param {ALT} alt
   * @param {ClothingStyle} clothing style
   * @param {Float} relevance of alt to given clothing style
   */
  private def addClothingStyles(alt: ALT, styles: ClothingStyles, relevance: Float) = {
    val rel: java.lang.Float = relevance
    val count: java.lang.Integer  = 1

    styles.styles foreach { style =>
      val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, style.name, rel, count)
      val iter  = Fun.filter(altsClothingStyle, alt.activity.value, alt.look.value, alt.timeWeather.value, style.name, rel).iterator
      if(iter.hasNext) {
        val existing = iter.next
        altsClothingStyle.remove(existing)
        val existingCount = existing(5).asInstanceOf[java.lang.Integer]
        val newCount: java.lang.Integer = existingCount.intValue() + 1
        entry(5) = newCount
      }
      altsClothingStyle.add(entry)
    }
  }

  /**
   * This function adds given {ALT}, {ItemTypeGroup} and corresponding relevance to the item type group
   *
   * @param {ALT} alt
   * @param {ItemTypeGroup} item type group
   * @param {Float} relevance of alt to given item type group
   */
  private def addItemTypeGroup(alt: ALT, itg: ItemTypeGroup, relevance: Float) = {
    val rel: java.lang.Float = relevance
    val count: java.lang.Integer = 1

    val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, itg.name, rel, count)
    val iter  = Fun.filter(altsItemTypeGroup, alt.activity.value, alt.look.value, alt.timeWeather.value, itg.name, rel).iterator
    if(iter.hasNext) {
      val existing = iter.next
      altsItemTypeGroup.remove(existing)
      val existingCount = existing(5).asInstanceOf[java.lang.Integer]
      val newCount: java.lang.Integer = existingCount.intValue() + 1
      entry(5) = newCount
    }
    altsItemTypeGroup.add(entry)
  }

  /**
   * This function extracts ALT, relevance and count from array stored in
   * MapDB
   *
   * @param {Array[Object]}
   * @param {Boolean} withAlt true if alt needs to be extracted
   */
  private def extract(arr: Array[Object], withAlt: Boolean) = {
    val rel = arr(4).asInstanceOf[java.lang.Float].floatValue()
    val count = arr(5).asInstanceOf[java.lang.Integer].intValue()
    if(withAlt) {
      val alt = ALT(Activity(arr(0).asInstanceOf[String]), Look(arr(1).asInstanceOf[String]), TimeWeather(arr(2).asInstanceOf[String]))
      (alt, rel, count)
    } else (rel, count)
  }

}

/**
 * Companion object
 */
object ALTItemRelevanceDataset {

  def apply(filePath: String): ALTItemRelevanceDataset = apply(makeDB(filePath))
  def apply(db: DB) = new ALTItemRelevanceDataset(db)

  val ITEMTYPEGROUPCLAZZ = classOf[ItemTypeGroup]
  val ITEMFEATURECLAZZ   = classOf[ItemFeature]
  val CLOTHINGSTYLECLAZZ = classOf[ClothingStyle]

}