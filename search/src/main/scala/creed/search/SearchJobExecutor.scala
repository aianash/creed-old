package creed
package search

import scala.util._

import query.SearchContext
import core.search.{SearchId, SearchResult}
import core.search.exception._

import akka.actor.{Actor, Props, ActorLogging, ReceiveTimeout, ActorSelection}


class SearchJobExecutor(job: SearchJob) extends Actor {

  import context.dispatcher
  import protocols._
  import job._

  val settings = SearchSettings(context.system)

  val cache = context.actorSelection("../cache")

  context.setReceiveTimeout(settings.SEARCH_RESULT_PROCESSING_TIMEOUT.duration)

  override def preStart(): Unit = searcher ! ExecuteSearchFor(searchId, searchContext)

  def receive = {

    case RankedItemIds(`searchId`, itemScores) =>
      implicit val timeout = settings.SEARCH_RESULT_PROCESSING_TIMEOUT
      job.makeSearchResult(itemScores) andThen {
        case Success(result) => job.completeWithResult(result)
        case Failure(ex) =>
          context.parent ! SearchException(SearchError.TooMuchLoad,
                                           "timedout while fetching item details",
                                           "Oops! we are experiencing a heavy load. Our team is already on it")
      }
      context stop self

    case TerminateJobExecution(`searchId`) =>
      searcher ! StopSearchingFor(searchId)


    case ReceiveTimeout =>
      searcher ! StopSearchingFor(searchId)
      context.parent ! SearchException(SearchError.TooMuchLoad,
                                       "timedout while waiting to search result to be computed",
                                       "Oops! We are experiencing a heavy load. Our team is already on it.")
      context stop self
  }

}

object SearchJobExecutor {
  def props(job: SearchJob) =
    Props(classOf[SearchJobExecutor], job)
}