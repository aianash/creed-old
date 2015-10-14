package creed
package core
package search
package protocols

import scala.language.existentials

import core.WaitFor
import client.search.{SearchId, SearchResult, QueryRecommendations}

import akka.actor.ActorRef


sealed trait SearchBackchannelMessages
case class RegisterBackchannelFor(searchId: SearchId, endpoint: ActorRef, clazz: Class[_ <: Any], waitFor: WaitFor) extends SearchBackchannelMessages
case class SendThruBackchannelFor(searchId: SearchId, message: Any) extends SearchBackchannelMessages
// case class SendQueryRecommendationsFor(searchId: SearchId, recommendations: QueryRecommendations) extends SearchBackchannelMessages
// case class SendSearchResultFor(searchId: SearchId, result: SearchResult) extends SearchBackchannelMessages