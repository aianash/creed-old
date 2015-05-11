package creed.core.query

sealed trait FieldQueryParams

case class ColorsQueryParams(values: Array[String]) extends FieldQueryParams
case class SizesQueryParams(values: Array[String]) extends FieldQueryParams
case class BrandQueryParams(values: Array[String]) extends FieldQueryParams
case class FabricQueryParams(values: Array[String]) extends FieldQueryParams
case class FitQueryParams(values: Array[String]) extends FieldQueryParams
case class StyleQueryParams(values: Array[String]) extends FieldQueryParams
case class PriceQueryParams(value: Map[String, Float]) extends FieldQueryParams
case class DescriptionQueryParams(text: String) extends FieldQueryParams
case class ItemTypeGroupsQueryParams(text: String) extends FieldQueryParams
case class NamedTypeQueryParams(text: String) extends FieldQueryParams
case class ProductTitleQueryField(text: String) extends FieldQueryParams