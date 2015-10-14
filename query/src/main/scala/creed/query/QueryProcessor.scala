package creed
package query

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Props, Actor, ActorLogging}


class QueryProcessor extends Actor with ActorLogging {

  import protocols._
  import core.search.protocols._

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
    case req: ProcessQueryFor     => alt ! req
    case req: GetSearchContextFor => cache forward req
    case req: QueryIsServedFor    => cache ! req
  }

}

object QueryProcessor {
  def props = Props(classOf[QueryProcessor])
}