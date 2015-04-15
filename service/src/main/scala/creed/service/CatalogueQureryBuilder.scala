package creed.service

import play.api.libs.json._

import org.apache.lucene.search._
import org.apache.lucene.index._

object CatalogueQueryBuilder {

  def build(key: String, filters: JsValue): Query = key match {
    case "color" =>
      filters.as[Array[String]].foldLeft(new BooleanQuery()) { (query, term) =>
        query.add(new TermQuery(new Term(key, term)), BooleanClause.Occur.SHOULD)
        query
      }

    case "size" =>
      filters.as[Array[String]].foldLeft(new BooleanQuery()) { (query, term) =>
        query.add(new TermQuery(new Term(key, term)), BooleanClause.Occur.SHOULD)
        query
      }

    case "brand" =>
      val term = filters.as[String]
      val query = new BooleanQuery()
      query.add(new TermQuery(new Term(key, term)), BooleanClause.Occur.SHOULD)
      query

    case "clothingType" =>
      val term = filters.as[String]
      val query = new BooleanQuery()
      query.add(new TermQuery(new Term(key, term)), BooleanClause.Occur.SHOULD)
      query

    case "description" =>
      val term = filters.as[String]
      val query = new BooleanQuery()
      query.add(new TermQuery(new Term(key, term)), BooleanClause.Occur.SHOULD)
      query
  }
}