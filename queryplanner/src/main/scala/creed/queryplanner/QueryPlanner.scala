package creed.queryplanner

import akka.actor.Actor
import akka.actor.Props

import scala.reflect.ClassTag

import org.apache.lucene.search._

import play.api.libs.json._

import goshoplane.commons.catalogue._
import com.goshoplane.creed.search._

import creed.core._, fields._, query._

class QueryPlanner extends Actor {

  import protocols._


  val fields = CatalogueItemFields[ClothingItem].get

  val ColorsClass         = classOf[Colors]
  val SizesClass          = classOf[Sizes]
  val BrandClass          = classOf[Brand]
  val ApparelFabricClass  = classOf[ApparelFabric]
  val ApparelFitClass     = classOf[ApparelFit]
  val ApparelStyleClass   = classOf[ApparelStyle]
  val PriceClass          = classOf[Price]
  val DescriptionClass    = classOf[Description]
  val ItemTypeGroupsClass = classOf[ItemTypeGroups]
  val NamedTypeClass      = classOf[NamedType]
  val ProductTitleClass   = classOf[ProductTitle]


  def receive = {

    case BuildQuery(request) => sender() ! getQuery(request)

    case _ =>

  }

  def getQuery(request: CatalogueSearchRequest) = {
    val booleanQuery = new BooleanQuery
    request.query.params.foreach(param => {
      implicit val tag = ClassTag(getAttributeClass(param._1))
      for {
        paramValue  <- (param._2.json.map(Json.parse(_)) orElse param._2.values.map(_.toArray) orElse param._2.value orElse param._2.queryMap).toLeft(new Exception("No parameter is specified.")).left
        queryParams <- getFieldQueryParams(paramValue).left
        query       <- fields.query(queryParams).left
      } yield {
        booleanQuery.add(query, BooleanClause.Occur.SHOULD)
      }
    })

    for {
      descriptionQuery    <- getQueryFromQueryText("description", request.query.queryText).left
      titleQuery          <- getQueryFromQueryText("productTitle", request.query.queryText).left
      namedTypeQuery      <- getQueryFromQueryText("namedType", request.query.queryText).left
    } {
      booleanQuery.add(descriptionQuery, BooleanClause.Occur.SHOULD)
      booleanQuery.add(titleQuery, BooleanClause.Occur.SHOULD)
      booleanQuery.add(namedTypeQuery, BooleanClause.Occur.SHOULD)
    }

    booleanQuery
  }

  def getFieldQueryParams[T: ClassTag](params: Any): Either[FieldQueryParams, Exception] =
    (implicitly[ClassTag[T]].runtimeClass, params) match {

      case (ColorsClass, values: Array[String])         => Left(ColorsQueryParams(values))
      case (ColorsClass, value: String)                 => Left(ColorsQueryParams(Array(value)))
      case (ColorsClass, jsValue: JsValue)              => Left(ColorsQueryParams(jsValue.as[Array[String]]))
      
      case (SizesClass, values: Array[String])          => Left(SizesQueryParams(values))
      case (SizesClass, value: String)                  => Left(SizesQueryParams(Array(value)))
      case (SizesClass, jsValue: JsValue)               => Left(SizesQueryParams(jsValue.as[Array[String]]))
      
      case (BrandClass, values: Array[String])          => Left(BrandQueryParams(values))
      case (BrandClass, value: String)                  => Left(BrandQueryParams(Array(value)))
      case (BrandClass, jsValue: JsValue)               => Left(BrandQueryParams(jsValue.as[Array[String]]))
      
      case (ApparelFabricClass, values: Array[String])  => Left(FabricQueryParams(values))
      case (ApparelFabricClass, value: String)          => Left(FabricQueryParams(Array(value)))
      case (ApparelFabricClass, jsValue: JsValue)       => Left(FabricQueryParams(jsValue.as[Array[String]]))
      
      case (ApparelFitClass, values: Array[String])     => Left(FitQueryParams(values))
      case (ApparelFitClass, value: String)             => Left(FitQueryParams(Array(value)))
      case (ApparelFitClass, jsValue: JsValue)          => Left(FitQueryParams(jsValue.as[Array[String]]))
      
      case (ApparelStyleClass, values: Array[String])   => Left(StyleQueryParams(values))
      case (ApparelStyleClass, value: String)           => Left(StyleQueryParams(Array(value)))
      case (ApparelStyleClass, jsValue: JsValue)        => Left(StyleQueryParams(jsValue.as[Array[String]]))
      
      case (PriceClass, value: String)                  => Left(PriceQueryParams(Map("min" -> 0, "max" -> value.toFloat)))
      case (PriceClass, queryMap: Map[String, String])  => Left(PriceQueryParams(queryMap map ( x => x._1 -> x._2.toFloat)))
      case (PriceClass, jsValue: JsValue)               => Left(PriceQueryParams(jsValue.as[Map[String, Float]]))
      
      case (DescriptionClass, text: String)             => Left(DescriptionQueryParams(text))
      
      case (ItemTypeGroupsClass, value: String)         => Left(ItemTypeGroupsQueryParams(Array(value)))
      case (ItemTypeGroupsClass, values: Array[String]) => Left(ItemTypeGroupsQueryParams(values))
      case (ItemTypeGroupsClass, jsValue: JsValue)      => Left(ItemTypeGroupsQueryParams(jsValue.as[Array[String]]))
      
      case (NamedTypeClass, text: String)               => Left(NamedTypeQueryParams(text))
      
      case (ProductTitleClass, text: String)            => Left(ProductTitleQueryParams(text))

      case _ => Right(new Exception("Unrecognized field with class ${classOf[T]}"))

    }

  def getAttributeClass(fieldName: String): Class[_] = fieldName match {
    case "sizes"          => classOf[Sizes]
    case "colors"         => classOf[Colors]
    case "price"          => classOf[Price]
    case "brand"          => classOf[Brand]
    case "fabric"         => classOf[ApparelFabric]
    case "fit"            => classOf[ApparelFit]
    case "style"          => classOf[ApparelStyle]
    case "productTitle"   => classOf[ProductTitle]
    case "description"    => classOf[Description]
    case "namedType"      => classOf[NamedType]
    case "itemTypeGroups" => classOf[ItemTypeGroups]
    case _                => classOf[Nothing]
  }

  def getQueryFromQueryText(field: String, text: String): Either[Query, Exception] = {
    implicit val tag = ClassTag(getAttributeClass(field))
    for {
      queryParam <- getFieldQueryParams(text).left
      query      <- fields.query(queryParam).left
    } yield query
  }

}

object QueryPlanner {

  def props = Props(classOf[QueryPlanner])

}