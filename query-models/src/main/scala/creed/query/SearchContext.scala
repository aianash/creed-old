package creed
package query

import client.search._

import org.apache.lucene.search.Query


case class SearchContext(searchId: SearchId) {
  def hasRecommendations = ???
  def recommendations: QueryRecommendations = ???
  def query: Query = ???
  def itemTraits: Seq[String] = ???
  def itemTypes: Seq[String] = ???

  def traitScoreFor(itemTrait: String, itemType: String) = 1.0f
  def ALTItemTypeScoreFor(itemType: String) = 1.0f
}