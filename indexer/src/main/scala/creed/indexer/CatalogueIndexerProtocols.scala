package creed.indexer

import creed.core._

sealed trait CatalogueIndexerMessage extends Serializable

case class IndexCatalogue(catalogueItem: CatalogueItem) extends CatalogueIndexerMessage