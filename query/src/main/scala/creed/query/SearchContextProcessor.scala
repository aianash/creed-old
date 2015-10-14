package creed
package query

import scala.collection.JavaConversions._

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Props, ActorLogging}

import core.search._


class SearchContextProcessor extends Actor with ActorLogging {
  import SearchContextProcessor._
  import protocols._, models._
  import context.dispatcher

  val settings = SearchSettings(context.system)

  val model = new SearchContextModel
  val cache = context.actorSelection("../cache")

  def receive = {

    case ProcessForSearchContext(searchId, query, styles) =>
      val searchContext = model.searchContext(searchId, query, styles)
      cache ! CacheFor(searchId, searchContext)

  }

}

object SearchContextProcessor {
  def props = Props(classOf[SearchContextProcessor])
  case object CleanUp
}