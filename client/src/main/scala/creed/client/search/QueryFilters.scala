package creed
package client
package search

import scala.reflect.ClassTag
import scala.reflect.NameTransformer._
import scala.util.matching.Regex

import play.api.libs.json._

import commons.catalogue.attributes._


case class QueryFilters(filters: QueryFilter*) {
  def filter[F: ClassTag] = {
    val clazz = implicitly[ClassTag[F]].runtimeClass
    filters.find(clazz.isInstance(_)).map(_.asInstanceOf[F])
  }
}

trait QueryFilter {

  def name: String = toString stripSuffix "Filter"

  def json: JsObject

  override def toString: String =
    ((getClass.getName stripSuffix MODULE_SUFFIX_STRING split '.').last split
      Regex.quote(NAME_JOIN_STRING)).last
}

case class ColorFilter(colors: String*) extends QueryFilter {
  def json = Json.obj(name -> Json.arr(colors))
}

case class SizesFilter(sizes: ClothingSize*) extends QueryFilter {
  def json = Json.obj(name -> Json.arr(sizes.map(_.name)))
}
