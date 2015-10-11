package creed
package core
package cassie

import akka.actor.{Props, Actor}

import commons.catalogue._, items._, attributes._

/** Should be added as dependency from cassie-core
  *
  */
class CassieClient extends Actor {

  // DUMMY for testing flow
  val brandId = BrandId(12345L)
  val itemId = CatalogueItemId(123456L)
  val variantId = VariantId(1234567L)

  val title = ProductTitle("mens polo neck tshirt")
  val namedType = NamedType("polo neck tshirt")
  val brand = Brand("Brand name")
  val price = Price(1234.0F)
  val sizes = ClothingSizes(Seq(ClothingSize.S))
  val colors = Colors(Seq("RED"))
  val itemStyles = ClothingStyles(Seq(ClothingStyle.TeesTop))
  val description = Description("hello")
  val stylingTips = StylingTips("asdfasdf")
  val gender = Male
  val images = Images("http://goshoplane.com", Seq("http://goshoplane.com"))
  val itemUrl = ItemUrl("http://goshoplane.com")

  val item =
    MensTShirt.builder.forBrand
              .ids(brandId, itemId, variantId)
              .title(title)
              .namedType(namedType)
              .clothing(brand, price, sizes, colors, itemStyles, description, stylingTips, gender, images, itemUrl)
              .build

  def receive = {
    case GetCatalogueItems(itemIds) => sender() ! Seq(item)
  }
}

object CassieClient {
  def props = Props(classOf[CassieClient])
}

import goshoplane.commons.core.protocols.Replyable

case class GetCatalogueItems(itemIds: Seq[CatalogueItemId]) extends Replyable[Seq[CatalogueItem]]