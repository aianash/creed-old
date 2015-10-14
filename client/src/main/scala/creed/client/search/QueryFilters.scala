package creed
package client
package search

import scala.reflect.ClassTag
import scala.reflect.NameTransformer._
import scala.util.matching.Regex

import play.api.libs.json._

import commons.catalogue._, attributes._


case class QueryFilters(filters: QueryFilter*) {
  def filter[F: ClassTag] = {
    val clazz = implicitly[ClassTag[F]].runtimeClass
    filters.find(clazz.isInstance(_)).map(_.asInstanceOf[F])
  }
}

object QueryFilters {

  import ClothingSize._
  import ItemTypeGroup._

  private val filterMap = Map(
    WomensTops -> QueryFilters(
      ColorFilter("red", "blue", "green"),
      SizesFilter(S, M, L, XL, XXL)
    )
  )

  def get(styles: Set[ClothingStyle]) =
    styles.foldLeft(Map.empty[ClothingStyle, QueryFilters]) { (filters, style) =>
      filters + (style -> filterMap(ClothingStyle.group(style)))
    }
}