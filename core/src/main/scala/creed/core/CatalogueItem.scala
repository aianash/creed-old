package creed.core

import com.goshoplane.common._

sealed trait CatalogueItem extends Serializable {
  def itemId: CatalogueItemId
}

case class ClothingItem(itemId: CatalogueItemId, color: Color, size: Size, brand: Brand, clothingType: ClothingType, description: Description) extends CatalogueItem