package creed.core

import com.goshoplane.common._

sealed trait CatalogueItem extends Serializable {
  def itemId: CatalogueItemId
}

case class ClothingItem(itemId: CatalogueItemId, color: Color, size: Size, brand: Brand, clothingType: ClothingType, description: Description) extends CatalogueItem

object Catalogue {
  def decode(byte: Array[Byte]) = {
    val id = CatalogueItemId(1L, StoreId(2L, StoreType.Clothing))
    println(new String(byte))
    ClothingItem(
      itemId = id,
      color = Color(Array.empty[String]),
      size = Size(Array.empty[String]),
      brand = Brand("levis"),
      clothingType = ClothingType("men"),
      description = Description("s"));
  }
}