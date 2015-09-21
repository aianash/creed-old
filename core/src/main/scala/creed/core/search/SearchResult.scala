package creed
package core
package search

case class ItemScore(itemId: CatalogueItemId, score: Float)

case class SearchResult(searchId: SearchId, items: Seq[CatalogueItem], itemScores: Seq[ItemScore])