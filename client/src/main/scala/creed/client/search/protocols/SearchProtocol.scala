package creed
package client
package search
package protocols

import goshoplane.commons.core.protocols._

import search.QueryRecommendations

import akka.actor.ActorRef


sealed trait SearchMessages
case class UpdateQueryFor(searchId: SearchId, query: Query) extends SearchMessages
case class QueryRecommendationsFor(searchId: SearchId, queryRecommendations: QueryRecommendations) extends SearchMessages

case class GetSearchResultFor(searchId: SearchId) extends SearchMessages

// Move this somewhere else
case class RegisterForNotifications(endpoint: ActorRef)
