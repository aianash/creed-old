package creed
package query

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Props, Actor}

import client.search.SearchId
import core.search.SearchSettings
import core.nlp.NLP
import query.protocols._
import query.models.alt.ALTModel


class QueryToALT extends Actor {

  val settings = SearchSettings(context.system)

  val recommender = context.actorSelection("../recommender")
  val alts = new ConcurrentHashMap[SearchId, ALT]
  val model = ALTModel(settings.ALT_MODEL_FILE)

  def receive = {
    case ProcessQueryFor(searchId, query) =>
      val simsets = model.simsets(NLP.nouns(query.queryStr))

       // foreach { simsets =>
       //  if(isNewFor(searchId, alt)) recommender ! RecommendFor(searchId, query, alt) }
  }

  private def isNewFor(searchId: SearchId, alt: ALT) =
    alts.get(searchId) match {
      case null                         => alts.put(searchId, alt); true
      case oldAlt if(oldAlt equals alt) => false
      case _                            => alts.put(searchId, alt); true
    }

}

object QueryToALT {
  def props = Props(classOf[QueryToALT])
}