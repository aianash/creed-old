package creed
package search

import scala.concurrent.Future
import scala.collection.mutable.SortedSet

import akka.actor.{Props, Actor, ActorLogging}
import akka.pattern.pipe

import org.apache.lucene.search.{IndexSearcher, TopScoreDocCollector}

import commons.catalogue._

import client.search._
import core.search.SearchSettings
import query.SearchContext


class Searcher(indxSearcher: IndexSearcher) extends Actor with ActorLogging {

  import protocols._
  import context.dispatcher

  val settings = SearchSettings(context.system)

  def receive = {
    case ExecuteSearchFor(searchId, searchContext) =>
      forkAndSearch(searchId, searchContext) pipeTo sender()
  }

  /** Description of function
    * [TODO] Handle failure
    */
  def forkAndSearch(searchId: SearchId, searchContext: SearchContext) = Future {
    RankedItemIds(searchId, itemScores = IndexedSeq(ItemScore(CatalogueItemId(123456L), 1.0f)))
    // val query = searchContext.query
    // val collector = TopScoreDocCollector.create(settings.SEARCH_RESULT_PAGE_SIZE)
    // indxSearcher.search(query, collector)
    // val hits = collector.topDocs().scoreDocs

    // val ordering = Ordering[(Float, Long)].on[ItemScore](x => (x.score, x.itemId.cuid))
    // val itemScores =
    //   hits.foldLeft(SortedSet.empty[ItemScore](ordering)) { (result, hit) =>
    //     val doc = indxSearcher.doc(hit.doc)
    //     val itemId = CatalogueItemId(doc.get("cuid").toLong)
    //     result += ItemScore(itemId, hit.score)
    //   }

    // RankedItemIds(searchId, itemScores = itemScores.toIndexedSeq)
  }

}

object Searcher {
  def props(indxSearcher: IndexSearcher) = Props(classOf[Searcher], indxSearcher)
}