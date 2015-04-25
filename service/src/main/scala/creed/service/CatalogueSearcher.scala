package creed.service

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool

import org.apache.lucene.search._
import org.apache.lucene.index.Term

import play.api.libs.json._

import com.goshoplane.creed.search._
import com.goshoplane.common._

/**
 * Actor to handle search requests
 * Takes IndexSearcher while constuction
 */
class CatalogueSearcher(searcher: IndexSearcher) extends Actor {

  import protocols._

  def receive = {
    case SearchCatalogue(request) =>
      val booleanQuery = getQuery(request)
      val numHits = request.pageIndex * request.pageSize
      val collector = TopScoreDocCollector.create(numHits, true)
      val startIndex = (request.pageIndex - 1) * request.pageSize

      searcher.search(booleanQuery, collector)
      val hits = collector.topDocs(startIndex, request.pageSize).scoreDocs

      val searchResults =
        hits.foldLeft (List.empty[CatalogueResultEntry]) { (topDocs, hit) =>
          val doc = searcher.doc(hit.doc)
          val itemId = CatalogueItemId(StoreId(doc.get("storeId").toLong), doc.get("itemId").toLong)
          val resultEntry = CatalogueResultEntry(itemId, CreedScore(hit.score))
          resultEntry :: topDocs
        }
      sender() ! CatalogueSearchResults(request.searchId, searchResults)
  }

  /**
   * Function to generate query given CatalogueSearchRequest
   */
  private def getQuery(request: CatalogueSearchRequest): BooleanQuery = {
    val booleanQuery = new BooleanQuery();
    request.query.params.foreach(param => {
      val paramJson = (param._2.json.map(Json.parse(_))) orElse (param._2.value.map(Json.toJson(_)))
      paramJson.map(CatalogueQueryBuilder.build(param._1, _)) foreach(query => booleanQuery.add(query, BooleanClause.Occur.SHOULD))
    })
    booleanQuery.add(CatalogueQueryBuilder.build("description", Json.toJson(request.query.queryText)), BooleanClause.Occur.SHOULD)
    booleanQuery
  }

}

/**
 * CatalogueSearcher companion object
 */
object CatalogueSearcher {

  def props(searcher: IndexSearcher): Props = RoundRobinPool(5).props(Props(classOf[CatalogueSearcher], searcher))

}