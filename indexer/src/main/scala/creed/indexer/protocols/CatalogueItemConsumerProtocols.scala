package creed.indexer.protocols

import creed.core._

sealed trait CatalogueItemConsumerMessage  extends Serializable

case class ReadNextCatalogueBatch(batchSize: Int) extends CatalogueItemConsumerMessage

case class NextBatch(batch: List[CatalogueItem]) extends CatalogueItemConsumerMessage