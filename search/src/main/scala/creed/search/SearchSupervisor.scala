package creed
package search

import scala.util.{Try, Success, Failure}

import java.nio.file.FileSystems

import core.cassie._

import akka.actor.{Props, Actor, ActorLogging, Status}
import akka.util.Timeout
import akka.pattern.pipe

import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher


/** Supervisor of search requests
  *
  * @param Parameter1 - blah blah
  * @return Return value - blah blah
  */
class SearchSupervisor extends Actor with ActorLogging {

  import goshoplane.commons.core.protocols.Implicits._
  import protocols._
  import core.search.protocols._
  import query._, protocols._

  import context.dispatcher

  val settings = SearchSettings(context.system)

  val indxSearcher = {
    val dir = FSDirectory.open(FileSystems.getDefault.getPath(settings.INDEX_DIR))
    val reader = DirectoryReader.open(dir)
    new IndexSearcher(reader)
  }

  val cassie         = context.actorOf(CassieClient.props, "cassie")
  val backchannel    = context.actorOf(SearchBackchannel.props, "backchannel")
  val searcher       = context.actorOf(Searcher.props(indxSearcher), "searcher")
  val scheduler      = context.actorOf(SearchScheduler.props(cassie), "scheduler")
  val queryProcessor = context.actorOf(QueryProcessor.props, "queryProcessor")

  context watch backchannel
  context watch searcher
  context watch scheduler
  context watch queryProcessor
  context watch cassie


  def receive = {

    /** Update query for a search id
      * Also notify backchannel for register is not present
      */
    case UpdateQueryFor(searchId, query) =>
      queryProcessor ! ProcessQueryFor(searchId, query)
      backchannel ! RegisterBackchannelFor(searchId, sender())


    /** Description of function
      * 1. ask scheduler if the result is calculated or wait
      * 2. send the result once obtained or timeout
      */
    case request @ GetSearchResultFor(searchId) =>
      implicit val timeout = settings.FETCH_SEARCH_CONTEXT_TIMEOUT
      for(searchContext <- queryProcessor ?= GetSearchContextFor(searchId)) {
        scheduler ! ScheduleSearchingFor(searchId, searchContext)
      }


    /** Get query filters
      * 1. get filters for the search id
      */
    case request: GetQueryFiltersFor => queryProcessor forward request

  }

}

object SearchSupervisor {
  def props = Props(classOf[SearchSupervisor])
}