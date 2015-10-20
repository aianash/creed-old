package creed
package query
package datasets

import scala.reflect.ClassTag
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io._
import scala.math.Ordered

import org.mapdb._

import play.api.libs.json._

import core._, utils.MapDBUtils

import commons.catalogue._, attributes._

/**
 * This class represents a data point for catalogue items
 * [TODO] Add brand, sizes etc in later stages
 */
case class DatasetItemFeature(
  itemTypeGroup: ItemTypeGroup,
  styles: Seq[ClothingStyle],
  fabric: ApparelFabric,
  fit: ApparelFit,
  colors: Colors,
  stylingTips: StylingTips,
  descr: Description) extends Ordered[DatasetItemFeature] {

  def compare(that: DatasetItemFeature) = {
    if((this.itemTypeGroup.name == that.itemTypeGroup.name) &&
      (this.styles.map(_.name) == that.styles.map(_.name)) &&
      (this.fabric.fabric == that.fabric.fabric) &&
      (this.fit.fit == that.fit.fit) &&
      (this.colors.values == that.colors.values)) 0 else 1
  }

  override def toString = {
    s"""
    ItemTypeGroup: ${itemTypeGroup.name}
    Styles: ${(styles map { x => x.name }).mkString(", ")}
    Fabric: ${fabric.fabric}
    Fit: ${fit.fit}
    Colors: ${colors.values.mkString(", ")}
    Description: ${descr.text}
    Styling Tips: ${stylingTips.text}
    """
  }

}

/**
 * This dataset represents the relevance of an ALT for a DatasetItemFeature. It also
 * represents the relevance of an ALT for a ItemTypeGroup and ClothingStyle
 *
 * @param {DB}     db
 * @param {String} filepath of catalogue item json file
 */
class ALTItemRelevanceDataset(db: DB, itemFile: String) {

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

  private val items        = Source.fromFile(itemFile).getLines.toList
  private val activities   = db.hashSetCreate("_activities").makeOrGet[String].asScala
  private val looks        = db.hashSetCreate("_looks").makeOrGet[String].asScala
  private val timeWeathers = db.hashSetCreate("_timeWeathers").makeOrGet[String].asScala

  /**
   * Alias of {add} function
   */
  def +=(entry: (ALT, DatasetItemFeature, Float)): Unit = add(entry)

  /**
   * This function is used to add entries to the dataset
   * @param {(ALT, DatasetItemFeature, Float)} Tuple consiting of ALT, DatasetItemFeature and
   *                                           Relevance
   */
  def add(input: (ALT, DatasetItemFeature, Float)): Unit = {
    val alts      = input._1
    val feature   = input._2
    val relevance = input._3
    addItemFeature(alts, feature, relevance)
    addClothingStyles(alts, feature.styles, relevance)
    addItemTypeGroup(alts, feature.itemTypeGroup, relevance)
  }

  /**
   * Iterator for given type T. T can be one of the following: {DatasetItemFeature},
   * {ClothingStyle} and {ItemTypeGroup}
   *
   * @param T Type parameter
   */
  def iterator[T: ClassTag]: Iterator[(ALT, T, Float, Int)] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]] match {
      case DATASETITEMFEATURECLAZZ =>
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
   * {DatasetItemFeature}, {ClothingStyle} and {ItemTypeGroup}
   *
   * @param T Type paramemeter
   */
  def iterator[T: ClassTag](alt: ALT): Iterator[(T, Float, Int)] =
    implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]] match {
      case DATASETITEMFEATURECLAZZ =>
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
   * Iterator for random combinations of {ALT} and {DatasetItemFeature}
   */
  def generateRandomALTFeatureCombo = {
    val altIter = (WindowedRandomIterator(activities) <+> looks |+| timeWeathers)((x, t) => ALT(Activity(x._1), Look(x._2), TimeWeather(t)))
    (altIter |+| items)((s, t) => {
      val itemJson = Json.parse(t)
      val itg      = ItemTypeGroup((itemJson \ "itemTypeGroup").as[String])
      val styles   = (itemJson \ "styles").as[Seq[String]] map { x => ClothingStyle(x) }
      val fabric   = ApparelFabric((itemJson \ "fabric").as[String])
      val fit      = ApparelFit((itemJson \ "fit").as[String])
      val color    = Colors((itemJson \ "colors").as[Seq[String]])
      val tips     = StylingTips((itemJson \ "stylingTips").as[String])
      val descr    = Description((itemJson \ "descr").as[String])
      (s, DatasetItemFeature(itg, styles, fabric, fit, color, tips, descr))
    })
  }

  /**
   * This function adds given {ALT}, {DatasetItemFeature} and corresponding relevance to the dataset
   *
   * @param {ALT} alt
   * @param {DatasetItemFeature} dataset item feature
   * @param {Float} relevance of alt for given dataset item feature
   */
  private def addItemFeature(alt: ALT, feature: DatasetItemFeature, relevance: java.lang.Float) = {
    val count: java.lang.Integer = 1

    val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, feature, relevance, count)
    val iter  = Fun.filter(altsItemFeature, alt.activity.value, alt.look.value, alt.timeWeather.value, feature, relevance).iterator
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
   * This function adds given {ALT}, {ClothingStyle} and corresponding relevance to the dataset
   *
   * @param {ALT} alt
   * @param {ClothingStyle} clothing style
   * @param {Float} relevance of alt for given clothing style
   */
  private def addClothingStyles(alt: ALT, styles: Seq[ClothingStyle], relevance: java.lang.Float) = {
    val count: java.lang.Integer  = 1

    styles foreach { style =>
      val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, style.name, relevance, count)
      val iter  = Fun.filter(altsClothingStyle, alt.activity.value, alt.look.value, alt.timeWeather.value, style.name, relevance).iterator
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
   * This function adds given {ALT}, {ItemTypeGroup} and corresponding relevance to the dataset
   *
   * @param {ALT} alt
   * @param {ItemTypeGroup} item type group
   * @param {Float} relevance of alt for given item type group
   */
  private def addItemTypeGroup(alt: ALT, itg: ItemTypeGroup, relevance: java.lang.Float) = {
    val count: java.lang.Integer = 1

    val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, itg.name, relevance, count)
    val iter  = Fun.filter(altsItemTypeGroup, alt.activity.value, alt.look.value, alt.timeWeather.value, itg.name, relevance).iterator
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

  def apply(filePath: String, itemFile: String): ALTItemRelevanceDataset = apply(makeDB(filePath), itemFile)
  def apply(db: DB, itemFile: String) = new ALTItemRelevanceDataset(db, itemFile)

  val ITEMTYPEGROUPCLAZZ      = classOf[ItemTypeGroup]
  val DATASETITEMFEATURECLAZZ = classOf[DatasetItemFeature]
  val CLOTHINGSTYLECLAZZ      = classOf[ClothingStyle]

}