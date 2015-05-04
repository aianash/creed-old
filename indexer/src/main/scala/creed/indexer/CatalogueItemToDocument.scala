package creed.indexer

import creed.core._

import goshoplane.commons.catalogue._

class CatalogueItemToDocument {

  object ClothingItemToDocument extends ClothingItemToDocument

  def convert(catalogueItem: CatalogueItem) =
    catalogueItem match {
      case item: ClothingItem => ClothingItemToDocument(item)
    }

}