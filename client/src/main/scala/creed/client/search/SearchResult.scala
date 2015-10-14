package creed
package client
package search

import commons.catalogue._, collection._

case class ItemScore(itemId: CatalogueItemId, score: Float)

case class SearchResult(searchId: SearchId, items: CatalogueItems, itemScores: Seq[ItemScore])