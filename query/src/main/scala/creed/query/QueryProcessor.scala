package creed
package query

import scala.concurrent.duration._

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Props, Actor, ActorLogging}


class QueryProcessor extends Actor with ActorLogging {

  import goshoplane.commons.core.protocols.Implicits._
  import protocols._
  import core.search.protocols._
  import context.dispatcher

  // back channel is used for sending query recommendations
  // to user once processing is finished
  val backchannel   = context.actorSelection("../backchannel")
  val cache         = context.actorOf(SearchContextCache.props, "cache")
  val recommender   = context.actorOf(QueryRecommender.props(backchannel), "recommender")
  val alt           = context.actorOf(QueryToALT.props, "alt")
  val searchContext = context.actorOf(SearchContextProcessor.props, "searchContext")

  // Register to get notifcation when search id's result is sent
  // override def preStart(): Unit =
    // backchannel ! RegisterForNotifications(self)

  def receive = {
    case req: GetSearchContextFor => cache forward req
    case req: QueryIsServedFor    => cache ! req
    case req: ProcessQueryFor     =>
      implicit val timeout = akka.util.Timeout(1 seconds)
      (cache ?= HasQueryStrChangedFor(req.searchId, req.query.queryStr)) foreach { hasChanged =>
        if(hasChanged) alt ! req
      }
  }

}

object QueryProcessor {
  def props = Props(classOf[QueryProcessor])
}