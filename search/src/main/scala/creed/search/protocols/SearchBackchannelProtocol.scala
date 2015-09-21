package creed
package search
package protocols

import core.search.{SearchId, SearchResult, QueryRecommendations}

import akka.actor.ActorRef


sealed trait SearchBackchannelMessages
case class RegisterBackchannelFor(searchId: SearchId, endpoint: ActorRef) extends SearchBackchannelMessages
case class SendQueryRecommendationsFor(searchId: SearchId, recommendations: QueryRecommendations) extends SearchBackchannelMessages
case class SendSearchResultFor(searchId: SearchId, result: SearchResult) extends SearchBackchannelMessages