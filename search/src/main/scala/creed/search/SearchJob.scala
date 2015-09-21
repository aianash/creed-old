package creed
package search

import scala.concurrent.{Promise, ExecutionContext}
import scala.concurrent.duration._

import goshoplane.commons.core.protocols.Implicits._
import query.SearchContext
import core.search._

import core.cassie._

import akka.actor.{ActorRef, ActorSelection}
import akka.util.Timeout

// TO DO add job status

case class SearchJob(searchId: SearchId, searchContext: SearchContext, searcher: ActorSelection, cassie: ActorRef) {

  import SearchJob._

  private val startTime = System.currentTimeMillis
  private val resultP = Promise[SearchResult]

  def onResult(f: SearchResult => Unit) = resultP.future.onSuccess {
    case result: SearchResult => f(result)
  } (executionContext)

  def id = s"u${searchId.userId.uuid}-sr${searchId.sruid}"

  def executionTime = (System.currentTimeMillis - startTime).milliseconds

  def elapsedDuration = (System.currentTimeMillis - startTime).milliseconds

  def completeWithResult(result: SearchResult) = resultP success result

  def makeSearchResult(itemScores: Seq[ItemScore])(implicit timeout: Timeout, ec: ExecutionContext) =
    for(items <- cassie ?= GetCatalogueItems(itemScores.map(_.itemId)))
      yield SearchResult(searchId, items, itemScores)


}

object SearchJob {
  val executionContext = ExecutionContext.global
}