package creed
package search

import core.CatalogueItemId
import core.search._

case class RankedItemIds(searchId: SearchId, itemScores: IndexedSeq[ItemScore])