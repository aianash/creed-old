package creed.indexer.protocols

import creed.core._

sealed trait CatalogueItemConsumerMessage  extends Serializable

case class ReadNextCatalogueItem(batchSize: Int) extends CatalogueItemConsumerMessage

case class NextBatch(batch: List[CatalogueItem]) extends CatalogueItemConsumerMessage