package creed
package query
package protocols

import core.query._, core.search._


trait QueryRecommenderMessages
case class RecommendFor(searchId: SearchId, query: Query, alt: ALT) extends QueryRecommenderMessages