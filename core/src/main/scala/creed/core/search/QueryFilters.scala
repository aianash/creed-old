package creed
package core
package search

import scala.reflect.ClassTag
import scala.reflect.NameTransformer._
import scala.util.matching.Regex

import play.api.libs.json._

import commons.catalogue.attributes._


case class QueryFilters(filters: Seq[QueryFilter]) {
  def filter[F: ClassTag] = {
    val clazz = implicitly[ClassTag[F]].runtimeClass
    filters.find(clazz.isInstance(_)).map(_.asInstanceOf[F])
  }
}

// object QueryFilters {
//   def apply(filters: QueryFilter*): QueryFilters = apply(filters: _*)
// }

trait QueryFilter {

  def name: String = toString stripSuffix "Filter"

  def json: JsObject

  override def toString: String =
    ((getClass.getName stripSuffix MODULE_SUFFIX_STRING split '.').last split
      Regex.quote(NAME_JOIN_STRING)).last
}

case class ColorFilter(colors: Seq[String]) extends QueryFilter {
  def json = Json.obj(name -> Json.arr(colors))
}

// object ColorFilter {
//   def apply(colors: String*): ColorFilter = apply(colors:_*)
// }

case class SizesFilter(sizes: Seq[ClothingSize]) extends QueryFilter {
  def json = Json.obj(name -> Json.arr(sizes.map(_.name)))
}

// object SizesFilter {
//   def apply(sizes: ClothingSize*): SizesFilter = apply(sizes:_*)
// }