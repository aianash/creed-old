package creed.core.fields

import scala.reflect.runtime.universe._
import scala.util.{Either, Right, Left}

import java.io.StringReader

import scalaz._, Scalaz._

import goshoplane.commons.catalogue._

import com.goshoplane.common._

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.search._
import org.apache.lucene.index.Term
import org.apache.lucene.util.Version
import org.apache.lucene.document._, Field._
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes._

import creed.core._, query._

 
class ClothingItemFields extends CatalogueItemFields[ClothingItem] {

  private val DEFAULT_ANALYZER = new StandardAnalyzer(Version.LUCENE_48)

  private val itemIdField         = new StringField(indexFieldName[CatalogueItemId].left.get, "", Field.Store.YES)
  private val storeIdField        = new StringField(indexFieldName[StoreId].left.get, "", Field.Store.YES)
  private val itemTypeGroupsField = new TokenizedField(indexFieldName[ItemTypeGroups].left.get, Field.Store.NO)
  private val colorField          = new TokenizedField(indexFieldName[Colors].left.get, Field.Store.NO)
  private val sizeField           = new TokenizedField(indexFieldName[Sizes].left.get, Field.Store.NO)
  private val brandField          = new StringField(indexFieldName[Brand].left.get, "", Field.Store.NO)
  private val productTitleField   = new TextField(indexFieldName[ProductTitle].left.get, "", Field.Store.NO)
  private val namedTypeField      = new TextField(indexFieldName[NamedType].left.get, "", Field.Store.NO)
  private val fabricField         = new TextField(indexFieldName[ApparelFabric].left.get, "", Field.Store.NO)
  private val fitField            = new TextField(indexFieldName[ApparelFit].left.get, "", Field.Store.NO)
  private val styleField          = new TextField(indexFieldName[ApparelStyle].left.get, "", Field.Store.NO)
  private val priceField          = new FloatField(indexFieldName[Price].left.get, 0L, Field.Store.NO)
  private val descriptionField    = new TextField(indexFieldName[Description].left.get, new StringReader(""))

  def analyzer[T: TypeTag] =
    typeOf[T] match {
      case t if t =:= typeOf[CatalogueItemId] => Left(None)
      case t if t =:= typeOf[StoreId]         => Left(None)
      case t if t =:= typeOf[Colors]          => Left(None)
      case t if t =:= typeOf[Sizes]           => Left(None)
      case t if t =:= typeOf[Brand]           => Left(None)
      case t if t =:= typeOf[Description]     => Left(DEFAULT_ANALYZER.some)
      case t if t =:= typeOf[ItemTypeGroups]  => Left(None)
      case t if t =:= typeOf[NamedType]       => Left(DEFAULT_ANALYZER.some)
      case t if t =:= typeOf[Price]           => Left(None)
      case t if t =:= typeOf[ProductTitle]    => Left(DEFAULT_ANALYZER.some)
      case t if t =:= typeOf[ApparelFit]      => Left(DEFAULT_ANALYZER.some)
      case t if t =:= typeOf[ApparelStyle]    => Left(DEFAULT_ANALYZER.some)
      case t if t =:= typeOf[ApparelFabric]   => Left(DEFAULT_ANALYZER.some)
      case _ => Right(FieldException(s"No analyzer for field ${typeOf[T]}"))
    }

  def perFieldAnalyzerEntry[T: TypeTag]: Either[(String, Analyzer), Exception] = 
    for {
      name     <- indexFieldName[T].left
      analyzer <- analyzer[T].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${typeOf[T]}"))).left
    } yield (name, analyzer)

  def perFieldAnalyzer = {
    val analyzer = DEFAULT_ANALYZER

    for {
      productTitle <- perFieldAnalyzerEntry[ProductTitle].left
      description  <- perFieldAnalyzerEntry[Description].left
      fabric       <- perFieldAnalyzerEntry[ApparelFabric].left
      fit          <- perFieldAnalyzerEntry[ApparelFit].left
      style        <- perFieldAnalyzerEntry[ApparelStyle].left
      namedType    <- perFieldAnalyzerEntry[NamedType].left
    } yield {
      val analyzerMap: java.util.Map[String, Analyzer] = new java.util.HashMap[String, Analyzer]()
      analyzerMap.put(productTitle._1, productTitle._2)
      analyzerMap.put(description._1, description._2)
      analyzerMap.put(fabric._1, fabric._2)
      analyzerMap.put(fit._1, fit._2)
      analyzerMap.put(style._1, style._2)
      analyzerMap.put(namedType._1, namedType._2)

      new PerFieldAnalyzerWrapper(analyzer, analyzerMap)
    }
  }

  def indexFieldName[T: TypeTag] =
    typeOf[T] match {
      case t if t =:= typeOf[CatalogueItemId] => Left("cuid")
      case t if t =:= typeOf[StoreId]         => Left("suid")
      case t if t =:= typeOf[Colors]          => Left("colors")
      case t if t =:= typeOf[Sizes]           => Left("sizes")
      case t if t =:= typeOf[Brand]           => Left("brand")
      case t if t =:= typeOf[Description]     => Left("description")
      case t if t =:= typeOf[ItemTypeGroups]  => Left("itemTypeGroups")
      case t if t =:= typeOf[NamedType]       => Left("namedType")
      case t if t =:= typeOf[Price]           => Left("price")
      case t if t =:= typeOf[ProductTitle]    => Left("productTitle")
      case t if t =:= typeOf[ApparelFit]      => Left("apparelFit")
      case t if t =:= typeOf[ApparelStyle]    => Left("apprelStyle")
      case t if t =:= typeOf[ApparelFabric]   => Left("apprelFabric")
      case _ => Right(FieldException(s"No index field name for field ${typeOf[T]}"))
    }

  def indexField[T](attribute: T) =
    attribute match {
      case itemId: CatalogueItemId        => itemIdField.setStringValue(itemId.cuid.toString); Left(itemIdField)
      case storeId: StoreId               => storeIdField.setStringValue(storeId.stuid.toString); Left(storeIdField)
      case colors: Colors                 => colorField.setTokens(colors.values); Left(colorField)
      case sizes: Sizes                   => sizeField.setTokens(sizes.values); Left(sizeField)
      case brand: Brand                   => brandField.setStringValue(brand.name); Left(brandField)
      case description: Description       => descriptionField.setReaderValue(new StringReader(description.text)); Left(descriptionField)
      case itemTypeGroups: ItemTypeGroups => itemTypeGroupsField.setTokens(itemTypeGroups.groups.map(_.toString)); Left(itemTypeGroupsField)
      case namedType: NamedType           => namedTypeField.setStringValue(namedType.name); Left(namedTypeField)
      case price: Price                   => priceField.setFloatValue(price.value); Left(priceField)
      case productTitle: ProductTitle     => productTitleField.setStringValue(productTitle.title); Left(productTitleField)
      case fit: ApparelFit                => fitField.setStringValue(fit.fit); Left(fitField)
      case style: ApparelStyle            => styleField.setStringValue(style.style); Left(styleField)
      case fabric: ApparelFabric          => fabricField.setStringValue(fabric.fabric); Left(fabricField)
      case _ => Right(FieldException(s"No index field for ${attribute.getClass}"))
    }

  def document(item: ClothingItem) = {
    for {
      itemId         <- indexField(item.itemId).left
      storeId        <- indexField(item.itemId.storeId).left
      brand          <- indexField(item.brand).left
      description    <- indexField(item.description).left
      productTitle   <- indexField(item.productTitle).left
      namedType      <- indexField(item.namedType).left
      colors         <- indexField(item.colors).left
      sizes          <- indexField(item.sizes).left
      itemTypeGroups <- indexField(item.itemTypeGroups).left
      fabric         <- indexField(item.fabric).left
      fit            <- indexField(item.fit).left
      price          <- indexField(item.price).left
      style          <- indexField(item.style).left
    } yield {
      val document = new Document()
      document add itemId
      document add storeId
      document add brand
      document add description
      document add productTitle
      document add namedType
      document add colors
      document add sizes
      document add itemTypeGroups
      document add fabric
      document add fit
      document add price
      document add style
      document
    }
  }

  /**
   * [param description]
   * @type {[type]}
   * @TODO remove repitition of code
   */
  def query(param: FieldQueryParams) =
    param match {
      case ColorsQueryParams(values) =>
        for {
          name <- indexFieldName[Colors].left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            query.add(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD)
            query
          }
        }

      case SizesQueryParams(values) =>
        for {
          name <- indexFieldName[Sizes].left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            query.add(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD)
            query
          }
        }

      case BrandQueryParams(values) =>
        for {
          name <- indexFieldName[Brand].left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            query.add(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD)
            query
          }
        }

      case FabricQueryParams(values) =>
        for {
          name <- indexFieldName[ApparelFabric].left
          analyzer <- analyzer[ApparelFabric].left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            val tokenStream = analyzer.get.tokenStream(name, value)
            val charTermAttr = tokenStream.addAttribute(classOf[CharTermAttribute])
            while(tokenStream.incrementToken) {
              query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
            }
            query
          }
        }

      case FitQueryParams(values) =>
        for {
          name <- indexFieldName[ApparelFit].left
          analyzer <- analyzer[ApparelFit].left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            val tokenStream = analyzer.get.tokenStream(name, value)
            val charTermAttr = tokenStream.addAttribute(classOf[CharTermAttribute])
            while(tokenStream.incrementToken) {
              query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
            }
            query
          }
        }

      case StyleQueryParams(values) =>
        for {
          name <- indexFieldName[ApparelStyle].left
          analyzer <- analyzer[ApparelStyle].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${ApparelStyle.getClass}"))).left
        } yield {
          values.foldLeft(new BooleanQuery) { (query, value) =>
            val tokenStream = analyzer.tokenStream(name, value)
            val charTermAttr = tokenStream.getAttribute(classOf[CharTermAttribute])
            while(tokenStream.incrementToken) {
              query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
            }
            query
          }
        }

      case PriceQueryParams(value) =>
        for {
          name <- indexFieldName[Price].left
        } yield {
          NumericRangeQuery.newFloatRange(name, value("min"), value("max"), true, true)
        }

      case DescriptionQueryParams(text) =>
        for {
          name <- indexFieldName[Description].left
          analyzer <- analyzer[Description].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${Description.getClass}"))).left
        } yield {
          val query = new BooleanQuery
          val tokenStream = analyzer.tokenStream(name, text)
          val charTermAttr = tokenStream.getAttribute(classOf[CharTermAttribute])
          while(tokenStream.incrementToken) {
            query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
          }
          query
        }

      case ItemTypeGroupsQueryParams(text) =>
        for {
          name <- indexFieldName[ItemTypeGroups].left
          analyzer <- analyzer[ItemTypeGroups].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${ItemTypeGroups.getClass}"))).left
        } yield {
          val query = new BooleanQuery
          val tokenStream = analyzer.tokenStream(name, text)
          val charTermAttr = tokenStream.getAttribute(classOf[CharTermAttribute])
          while(tokenStream.incrementToken) {
            query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
          }
          query
        }

      case NamedTypeQueryParams(text) =>
        for {
          name <- indexFieldName[NamedType].left
          analyzer <- analyzer[NamedType].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${NamedType.getClass}"))).left
        } yield {
          val query = new BooleanQuery
          val tokenStream = analyzer.tokenStream(name, text)
          val charTermAttr = tokenStream.getAttribute(classOf[CharTermAttribute])
          while(tokenStream.incrementToken) {
            query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
          }
          query
        }

      case ProductTitleQueryField(text) =>
        for {
          name <- indexFieldName[ProductTitle].left
          analyzer <- analyzer[ProductTitle].left.flatMap(_.toLeft(FieldException(s"No analyzer found for ${ProductTitle.getClass}"))).left
        } yield {
          val query = new BooleanQuery
          val tokenStream = analyzer.tokenStream(name, text)
          val charTermAttr = tokenStream.getAttribute(classOf[CharTermAttribute])
          while(tokenStream.incrementToken) {
            query.add(new TermQuery(new Term(name, charTermAttr.toString)), BooleanClause.Occur.SHOULD)
          }
          query
        }

      case _ => Right(FieldException(s"No query for field for ${param.getClass}"))
    }

}