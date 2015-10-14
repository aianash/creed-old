package creed
package query

import scala.collection.JavaConversions._

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Props, ActorLogging}

import core._, search._


class SearchContextCache extends Actor with ActorLogging {
  import SearchContextCache._
  import protocols._
  import context.dispatcher

  val settings = SearchSettings(context.system)

  val cache = new ConcurrentHashMap[SearchId, (SearchContext, WaitFor)]()

  context.system.scheduler.schedule(settings.SEARCH_CONTEXT_CLEANUP_DELAY,
                                    settings.SEARCH_CONTEXT_CLEANUP_INTERVAL,
                                    self,
                                    CleanUp)

  def receive = {

    case CacheFor(searchId, searchContext) =>
      cache.put(searchId, searchContext -> WaitFor(settings.WAIT_FOR_SEARCH_CONTEXT).start)

    case GetSearchContextFor(searchId) =>
      cache.get(searchId) match {
        case null => akka.actor.Status.Failure(new Exception("No search context for this search id")) // doesnt not handle the delayed case
        case context => sender() ! context
      }

    case CleanUp =>
      val removals = cache.filter(_._2._2.expired).map(_._1)
      removals foreach(cache remove _)

  }

}

object SearchContextCache {
  def props = Props(classOf[SearchContextCache])
  case object CleanUp
}