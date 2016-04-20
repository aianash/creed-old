package creed
package search

import akka.actor.{Props, Actor, ActorLogging, Status}


/** Receives search request from other nodes (primarily higgs) on cluster
  */
class SearchService extends Actor with ActorLogging {

  import client.search.protocols._

  val supervisor = context.actorOf(SearchSupervisor.props, "supervisor")
  context watch supervisor

  def receive = {
    case msg : UpdateQueryFor => supervisor forward msg
    case msg : GetSearchResultFor => supervisor forward msg
    case msg => log.warning("Received unsupported message {}", msg)
  }

}


object SearchService {
  def props = Props(classOf[SearchService])
}