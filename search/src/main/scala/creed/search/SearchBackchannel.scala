package creed
package search

import akka.actor.{Props, Actor, ActorLogging, Terminated}


class SearchBackchannel(settings: SearchSettings) extends Actor with ActorLogging {

  import core.search.protocols._
  import protocols._

  def receive = {

    case RegisterBackchannelFor(searchId, endpoint) =>

    case SendQueryRecommendationsFor(searchId, recommendations) =>

    case SendSearchResultFor(searchId, result) =>

    case RegisterForNotifications(endpoint) =>
      context watch endpoint

    case Terminated(endpoint) =>

  }

}

object SearchBackchannel {
  def props = Props(classOf[SearchBackchannel])
}