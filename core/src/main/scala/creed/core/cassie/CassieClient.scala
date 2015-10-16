package creed
package core
package cassie

import akka.actor.{Props, Actor}

import commons.owner.{Brand => _, _}
import commons.catalogue._, items._, attributes._, collection._


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
  val fabric = ApparelFabric("cotton")
  val fit = ApparelFit("slim fit")

  val brandItem =
    MensTShirt.builder.forBrand
              .ids(brandId, itemId, variantId)
              .title(title)
              .namedType(namedType)
              .clothing(brand, price, sizes, colors, itemStyles, description, stylingTips, gender, images, itemUrl, fit, fabric)
              .build

  val item =
    MensTShirt.builder.forStore.using(brandItem)
              .ids(StoreId(102934L), itemId, variantId)
              .build

  def receive = {
    case GetCatalogueItems(itemIds) => sender() ! CatalogueItems(Seq(item))
  }
}

object CassieClient {
  def props = Props(classOf[CassieClient])


//   import ClothingStyle._
//   val qfilters = QueryFilters(List(ColorFilter(Seq("red", "blue", "green")), SizesFilter(Seq("XS", "2XS", "3XS"))))
//   val qfilters2 = QueryFilters(List(ColorFilter(Seq("pink", "blue", "green")), SizesFilter(Seq("XS", "2XS", "3XS"))))
//   val queryRecc = QueryRecommendations(
//     styles  = List(TeesTop, BodysuitTop, CropTop),
//     filters = Map(TeesTop -> qfilters, BodysuitTop -> qfilters2, CropTop -> qfilters)
//   )

//   val item1 = Json.obj(
//     "cuid" -> "199282919",
//     "title" -> "Blue Tiara Top",
//     "price" -> 549f,
//     "sizes" -> Json.arr("3XS", "2XS", "XS"),
//     "colors" -> Json.arr("Blue", "Black"),
//     "brand" -> Json.obj(
//       "id" -> "192881",
//       "name" -> "StalkBuyLove"
//     ),
//     "descr" -> """
// Women's fashion top made with stretchable knit cotton spandex
// Color block design
// Spaghetti shoulder straps
// Our European styles are designed in-house by our highly qualified designers.
// We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
// We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
// We do not offer high discounts in order to sustain high quality standards and designs that we provide.
//     """,
//     "stylingTips" -> "Va Va Voom for this sexy top! This one is our personal favourite cause it has everything to complete a sexy look. A funky neckline- check!, spagetti straps - check!, figure hugging and soft material - check! Pair this top us with a colouful pair of skinny jeans; the yellow and blue combination is trending a lot lately so maybe you can give that a try. Dont wear a neck piece with this top. Let the focus be on the gorgeous neckline.",
//     "images" -> Json.arr(
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-option.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-ghost.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-front.jpg"),
//     "primaryImage" -> "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-front.jpg",
//     "stores" -> Json.arr(
//         Json.obj(
//           "storeId" -> "12",
//           "name" -> "StalkBuyLove",
//           "url" -> "http://www.stalkbuylove.com/blue-tiara-top-83475-SBLPR.html")
//     ),
//     "styles" -> Json.arr("Spaghetti Top"),
//     "itemTypeGroup" -> "Tops",
//     "groups" -> Json.arr("Clothing", "Womens Clothing", "Womens Tops")
//   )

//   val item2 = Json.obj(
//     "cuid" -> "1992829192",
//     "title" -> "Peachy Pop Up Top",
//     "price" -> 549f,
//     "sizes" -> Json.arr("3XS", "2XS", "XS"),
//     "colors" -> Json.arr("Peach", "Mint", "Blue", "Green"),
//     "brand" -> Json.obj(
//       "id" -> "192881",
//       "name" -> "StalkBuyLove"
//     ),
//     "descr" -> """
// Women's fashion top made with stretchable knit cotton spandex
// Color block design
// Spaghetti shoulder straps
// Our European styles are designed in-house by our highly qualified designers.
// We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
// We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
// We do not offer high discounts in order to sustain high quality standards and designs that we provide.
//     """,
//     "stylingTips" -> "This Peachy Pop Up top is made with polyester crepe, and has a colour block design. The elasticized sleeve openings and tie up straps at the back are its defining features. Wear this with your favourite pair of jeggings or jeans to finish this look!",
//     "images" -> Json.arr(
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-front-sbl_1.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-back-sbl_1.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-option-sbl_1.jpg"),
//     "primaryImage" -> "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-front-sbl_1.jpg",
//     "stores" -> Json.arr(
//         Json.obj(
//           "storeId" -> "12",
//           "name" -> "StalkBuyLove",
//           "url" -> "http://www.stalkbuylove.com/peachy-pop-up-top-78135-SBLPR.html")
//     ),
//     "styles" -> Json.arr("Crop Top"),
//     "itemTypeGroup" -> "Tops",
//     "groups" -> Json.arr("Clothing", "Womens Clothing", "Womens Tops")
//   )

//   val item3 = Json.obj(
//     "cuid" -> "1992821919",
//     "title" -> "Untie & Reveal Top",
//     "price" -> 549f,
//     "sizes" -> Json.arr("3XS", "2XS", "XS"),
//     "colors" -> Json.arr("Pink"),
//     "brand" -> Json.obj(
//       "id" -> "192881",
//       "name" -> "StalkBuyLove"
//     ),
//     "descr" -> """
// Women's fashion top made with stretchable knit cotton spandex
// Color block design
// Spaghetti shoulder straps
// Our European styles are designed in-house by our highly qualified designers.
// We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
// We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
// We do not offer high discounts in order to sustain high quality standards and designs that we provide.
//     """,
//     "stylingTips" -> "Va Va Voom for this sexy top! This one is our personal favourite cause it has everything to complete a sexy look. A funky neckline- check!, spagetti straps - check!, figure hugging and soft material - check! Pair this top us with a colouful pair of skinny jeans; the yellow and blue combination is trending a lot lately so maybe you can give that a try. Dont wear a neck piece with this top. Let the focus be on the gorgeous neckline.",
//     "images" -> Json.arr(
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-front.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-back.jpg",
//       "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-option.jpg"),
//     "primaryImage" -> "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-front.jpg",
//     "stores" -> Json.arr(
//         Json.obj(
//           "storeId" -> "12",
//           "name" -> "StalkBuyLove",
//           "url" -> "http://www.stalkbuylove.com/untie-reveal-top-73263-SBLPR.html")
//     ),
//     "styles" -> Json.arr("Crop Top"),
//     "itemTypeGroup" -> "Tops",
//     "groups" -> Json.arr("Clothing", "Womens Clothing", "Womens Tops")
//   )

}

import goshoplane.commons.core.protocols.Replyable

case class GetCatalogueItems(itemIds: Seq[CatalogueItemId]) extends Replyable[CatalogueItems]