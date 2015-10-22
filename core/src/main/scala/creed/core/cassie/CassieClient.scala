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
  val brandId1 = BrandId(12345L)
  val itemId1 = CatalogueItemId(199282919L)
  val variantId1 = VariantId(199282919L)

  val title1 = ProductTitle("Blue Tiara Top")
  val namedType1 = NamedType("Tops")
  val brand1 = Brand("StalkBuyLove")
  val price1 = Price(1234.0F)
  val sizes1 = ClothingSizes(Seq(ClothingSize.S, ClothingSize.XS))
  val colors1 = Colors(Seq("Blue", "Black"))
  val itemStyles1 = ClothingStyles(Seq(ClothingStyle.SpaghettiTop))
  val description1 = Description("""
    Women's fashion top made with stretchable knit cotton spandex
    Color block design
    Spaghetti shoulder straps
    Our European styles are designed in-house by our highly qualified designers.
    We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
    We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
    We do not offer high discounts in order to sustain high quality standards and designs that we provide.
    """)
  val stylingTips1 = StylingTips("Va Va Voom for this sexy top! This one is our personal favourite cause it has everything to complete a sexy look. A funky neckline- check!, spagetti straps - check!, figure hugging and soft material - check! Pair this top us with a colouful pair of skinny jeans; the yellow and blue combination is trending a lot lately so maybe you can give that a try. Dont wear a neck piece with this top. Let the focus be on the gorgeous neckline.")
  val gender1 = Female
  val images1 = Images("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-front.jpg",
    Seq("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-option.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-ghost.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1514mtotopblu-131-front.jpg"))
  val itemUrl1 = ItemUrl("http://www.stalkbuylove.com/blue-tiara-top-83475-SBLPR.html")
  val fabric1 = ApparelFabric("Cotton")
  val fit1 = ApparelFit("Regular")

  val brandItem1 =
    WomensTops.builder.forBrand
              .ids(brandId1, itemId1, variantId1)
              .title(title1)
              .namedType(namedType1)
              .clothing(brand1, price1, sizes1, colors1, itemStyles1, description1, stylingTips1, gender1, images1, itemUrl1, fit1, fabric1)
              .build

  val item1 =
    WomensTops.builder.forStore.using(brandItem1)
              .ids(StoreId(102934L), itemId1, variantId1)
              .build

  val brandId2 = BrandId(12345L)
  val itemId2 = CatalogueItemId(1992829192L)
  val variantId2 = VariantId(1992829192L)

  val title2 = ProductTitle("Peachy Pop Up Top")
  val namedType2 = NamedType("Tops")
  val brand2 = Brand("StalkBuyLove")
  val price2 = Price(1234.0F)
  val sizes2 = ClothingSizes(Seq(ClothingSize.S, ClothingSize.XS, ClothingSize.X2S, ClothingSize.X3S))
  val colors2 = Colors(Seq("Peach", "Mint", "Blue", "Green"))
  val itemStyles2 = ClothingStyles(Seq(ClothingStyle.SpaghettiTop))
  val description2 = Description("""Women's fashion top made with stretchable knit cotton spandex Color block design Spaghetti shoulder straps. Our European styles are designed in-house by our highly qualified designers.
    We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
    We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
    We do not offer high discounts in order to sustain high quality standards and designs that we provide.""")
  val stylingTips2 = StylingTips("This Peachy Pop Up top is made with polyester crepe, and has a colour block design. The elasticized sleeve openings and tie up straps at the back are its defining features. Wear this with your favourite pair of jeggings or jeans to finish this look!")
  val gender2 = Female
  val images2 = Images("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-front-sbl_1.jpg",
    Seq("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-front-sbl_1.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-back-sbl_1.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1432mtotoppnk-345-option-sbl_1.jpg"))
  val itemUrl2 = ItemUrl("http://www.stalkbuylove.com/peachy-pop-up-top-78135-SBLPR.html")
  val fabric2 = ApparelFabric("Cotton")
  val fit2 = ApparelFit("Regular")

  val brandItem2 =
    WomensTops.builder.forBrand
              .ids(brandId2, itemId2, variantId2)
              .title(title2)
              .namedType(namedType2)
              .clothing(brand2, price2, sizes2, colors2, itemStyles2, description2, stylingTips2, gender2, images2, itemUrl2, fit2, fabric2)
              .build

  val item2 =
    WomensTops.builder.forStore.using(brandItem2)
              .ids(StoreId(102934L), itemId2, variantId2)
              .build

  val brandId3 = BrandId(12345L)
  val itemId3 = CatalogueItemId(1992821919L)
  val variantId3 = VariantId(1992821919L)

  val title3 = ProductTitle("Untie & Reveal Top")
  val namedType3 = NamedType("Tops")
  val brand3 = Brand("StalkBuyLove")
  val price3 = Price(1234.0F)
  val sizes3 = ClothingSizes(Seq(ClothingSize.S, ClothingSize.XS, ClothingSize.X2S, ClothingSize.X3S))
  val colors3 = Colors(Seq("Pink"))
  val itemStyles3 = ClothingStyles(Seq(ClothingStyle.CropTop))
  val description3 = Description("""
    Women's fashion top made with stretchable knit cotton spandex
    Color block design
    Spaghetti shoulder straps
    Our European styles are designed in-house by our highly qualified designers.
    We use high-quality fabrics and trendiest colours to ensure that you look fabulous.
    We promise that you will look fabulous in this style. If not, you can return the product within 7 days no-questions-asked!
    We do not offer high discounts in order to sustain high quality standards and designs that we provide.""")
  val stylingTips3 = StylingTips("Va Va Voom for this sexy top! This one is our personal favourite cause it has everything to complete a sexy look. A funky neckline- check!, spagetti straps - check!, figure hugging and soft material - check! Pair this top us with a colouful pair of skinny jeans; the yellow and blue combination is trending a lot lately so maybe you can give that a try. Dont wear a neck piece with this top. Let the focus be on the gorgeous neckline.")
  val gender3 = Female
  val images3 = Images("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-front.jpg",
    Seq("http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-front.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-back.jpg",
      "http://img.stalkbuylove.com/media/catalog/product/cache/1/image/870x1100/9df78eab33525d08d6e5fb8d27136e95/i/n/in1504mtotoppnk-108-option.jpg"))
  val itemUrl3 = ItemUrl("http://www.stalkbuylove.com/untie-reveal-top-73263-SBLPR.html")
  val fabric3 = ApparelFabric("Cotton")
  val fit3 = ApparelFit("Regular")

  val brandItem3 =
    WomensTops.builder.forBrand
              .ids(brandId3, itemId3, variantId3)
              .title(title3)
              .namedType(namedType3)
              .clothing(brand3, price3, sizes3, colors3, itemStyles3, description3, stylingTips3, gender3, images3, itemUrl3, fit3, fabric3)
              .build

  val item3 =
    WomensTops.builder.forStore.using(brandItem3)
              .ids(StoreId(103934L), itemId3, variantId3)
              .build

  def receive = {
    case GetCatalogueItems(itemIds) => sender() ! CatalogueItems(Seq(item1, item2, item3))
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