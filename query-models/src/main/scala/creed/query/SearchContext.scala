package creed
package query

import client.search._

import org.apache.lucene.search.{Query => LQuery}


case class SearchContext(searchId: SearchId, query: Query) {
  def hasRecommendations = ???
  def recommendations: QueryRecommendations = ???
  def lquery: LQuery = ???
  def itemTraits: Seq[String] = ???
  def itemTypes: Seq[String] = ???

  def traitScoreFor(itemTrait: String, itemType: String) = 1.0f
  def ALTItemTypeScoreFor(itemType: String) = 1.0f
}