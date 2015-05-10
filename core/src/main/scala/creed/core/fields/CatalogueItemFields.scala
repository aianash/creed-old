package creed.core.fields

import scala.reflect.runtime.universe._
import scala.util.{Either, Right, Left}

import scalaz._, Scalaz._

import goshoplane.commons.catalogue._

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.search._
import org.apache.lucene.util.Version
import org.apache.lucene.document._, Field._
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer

object CatalogueItemFields {

  def apply[T <: CatalogueItem : TypeTag] =
    typeOf[T] match {
      case t if t =:= typeOf[ClothingItem] => (new ClothingItemFields).some
      case _ => None
    }
}

trait CatalogueItemFields[CI <: CatalogueItem] {

  def analyzer[T: TypeTag]: Either[Option[Analyzer], Exception]
  def perFieldAnalyzer: Either[PerFieldAnalyzerWrapper, Exception]
  def indexFieldName[T: TypeTag]: Either[String, Exception]
  def indexField[T: TypeTag](attribute: T): Either[Field, Exception]
  def query[T: TypeTag](param: Any): Either[Query, Exception]
  def document(item: CI): Either[Document, Exception]

}
