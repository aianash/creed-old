package creed.service

import scala.concurrent._, duration._
import scala.util._
import scala.util.control.NonFatal

import akka.actor.{Actor, Props, ActorLogging, ActorRef}
import akka.routing.RoundRobinPool
import akka.pattern.pipe
import akka.util.Timeout

import org.apache.lucene.search._
import org.apache.lucene.index.Term

import play.api.libs.json._

import com.goshoplane.creed.search._
import com.goshoplane.common._

import goshoplane.commons.core.protocols.Implicits._

import creed.queryplanner.protocols._

/**
 * Actor to handle search requests
 * Takes IndexSearcher while constuction
 */
class CatalogueSearcher(searcher: IndexSearcher, queryPlanner: ActorRef) extends Actor with ActorLogging {

  import protocols._
  import context.dispatcher

  implicit val defaultTimeout = Timeout(1 seconds)

  def receive = {
    case SearchCatalogue(request) =>
      val resultsF =
        for {
          booleanQuery <- queryPlanner ?= BuildQuery(request)
          searchResults <- search(booleanQuery, request.pageIndex, request.pageSize)
        } yield CatalogueSearchResults(searchId = request.searchId, results = searchResults)

      resultsF andThen {
        case Failure(NonFatal(ex)) =>
          log.error(ex, s"Error getting search results for request ${request}")
      } pipeTo sender()
  }

  private def search(query: BooleanQuery, pageIndex: Int, pageSize: Int) = Future {
    val numHits = pageIndex * pageSize
    val collector = TopScoreDocCollector.create(numHits, true)
    val startIndex = (pageIndex - 1) * pageSize

    searcher.search(query, collector)
    val hits = collector.topDocs(startIndex, pageSize).scoreDocs

    hits.foldLeft (List.empty[CatalogueResultEntry]) { (topDocs, hit) =>
      val doc = searcher.doc(hit.doc)
      val itemId = CatalogueItemId(StoreId(doc.get("storeId").toLong), doc.get("itemId").toLong)
      val resultEntry = CatalogueResultEntry(itemId, CreedScore(hit.score))
      resultEntry :: topDocs
    }
  }

}

/**
 * CatalogueSearcher companion object
 */
object CatalogueSearcher {

  def props(searcher: IndexSearcher, queryPlanner: ActorRef): Props =
    RoundRobinPool(5).props(Props(classOf[CatalogueSearcher], searcher, queryPlanner))

}