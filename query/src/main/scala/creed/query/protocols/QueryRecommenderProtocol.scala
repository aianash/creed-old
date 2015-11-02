package creed
package query
package protocols

import client.search._

import models.alt.ScoredSimSetIds

trait QueryRecommenderMessages
case class RecommendFor(searchId: SearchId, query: Query, simsets: (ScoredSimSetIds[Activity], ScoredSimSetIds[Look], ScoredSimSetIds[TimeWeather]))
  extends QueryRecommenderMessages