package creed
package query

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Props, Actor}

import core.query._, core.search._
import protocols._, models._


class QueryToALT extends Actor {

  val recommender = context.actorSelection("../alt")
  val alts = new ConcurrentHashMap[SearchId, ALT]
  val model = new QueryALTModel

  def receive = {
    case ProcessQueryFor(searchId, query) =>
      model.alt(query) foreach(alt =>
        if(isNewFor(searchId, alt)) recommender ! RecommendFor(searchId, query, alt))
  }

  private def isNewFor(searchId: SearchId, alt: ALT) =
    alts.get(searchId) match {
      case null => alts.put(searchId, alt); true
      case oldAlt if(oldAlt equals alt) => false
      case _ => alts.put(searchId, alt); true
    }

}

object QueryToALT {
  def props = Props(classOf[QueryToALT])
}