package creed
package query
package protocols

import client.search._

import models.alt.ScoredSimSetIds

trait QueryRecommenderMessages
case class RecommendFor(searchId: SearchId, query: Query, simsets: (ScoredSimSetIds[Intent[Activity]], ScoredSimSetIds[Intent[Look]], ScoredSimSetIds[Intent[TimeWeather]]))
  extends QueryRecommenderMessages