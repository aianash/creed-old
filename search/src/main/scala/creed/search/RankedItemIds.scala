package creed
package search

import commons.catalogue.CatalogueItemId
import client.search._

case class RankedItemIds(searchId: SearchId, itemScores: IndexedSeq[ItemScore])