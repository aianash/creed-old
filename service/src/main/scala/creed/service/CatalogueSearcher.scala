package creed.service

import akka.actor.{Actor, Props}

import org.apache.lucene.search._
import org.apache.lucene.index.Term

import play.api.libs.json._

import com.goshoplane.creed.search._
import com.goshoplane.common._

class CatalogueSearcher(searcher: IndexSearcher) extends Actor {

  import creed.service.protocols._

  def receive = {
    case SearchCatalogue(request) =>
      val booleanQuery = getQuery(request)
      val collector = TopScoreDocCollector.create(100, true)
      val startIndex = (request.pageIndex - 1) * request.pageSize
      val results = searcher.search(booleanQuery, collector)
      val hits = collector.topDocs(startIndex, request.pageSize).scoreDocs
      hits.foldLeft (List.empty[CatalogueResultEntry]) { (topDocs, hit) =>
        val doc = searcher.doc(hit.doc)
        val itemId = CatalogueItemId(doc.get("itemId").toLong, StoreId(doc.get("storeId").toLong, StoreType(2)))
        val resultEntry = CatalogueResultEntry(itemId, CreedScore(hit.score))
        resultEntry :: topDocs
      }
  }

  private def getQuery(request: CatalogueSearchRequest) = {
    val booleanQuery = new BooleanQuery();
    request.query.params.foreach(param => {
      val paramJson = (param._2.json.map(Json.parse(_))) orElse (param._2.value.map(Json.toJson(_)))
      paramJson.map(CatalogueQueryBuilder.build(param._1, _)) foreach(query => booleanQuery.add(query, BooleanClause.Occur.SHOULD))
    })
    booleanQuery
  }

}

object CatalogueSearcher {

  def props(searcher: IndexSearcher) = Props(new CatalogueSearcher(searcher))

}