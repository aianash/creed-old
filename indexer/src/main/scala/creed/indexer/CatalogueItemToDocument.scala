package creed.indexer

import creed.core._

import org.apache.lucene.document._
import org.apache.lucene.document.Field._

import java.io.StringReader

import goshoplane.commons.catalogue._

class CatalogueItemToDocument {

  private val itemIdField       = new StringField("itemId", "", Field.Store.YES)
  private val storeIdField      = new StringField("storeId", "", Field.Store.YES)
  private val colorField        = new StringField("color", "", Field.Store.NO)
  private val sizeField         = new StringField("size", "", Field.Store.NO)
  private val brandField        = new StringField("brand", "", Field.Store.NO)
  private val clothingTypeField = new StringField("clothingType", "", Field.Store.NO)
  private val productTitleField = new StringField("productTitle", "", Field.Store.NO)
  private val namedTypeField    = new StringField("namedType", "", Field.Store.NO)
  private val descriptionField  = new TextField("description", new StringReader(""))

  def convert(catalogueItem: CatalogueItem) =
    catalogueItem match {
      case ClothingItem(itemId, itemType, itemTypeGroups, namedType, productTitle, colors, sizes, brand, description, price) =>
        itemIdField.setStringValue(itemId.cuid.toString)
        storeIdField.setStringValue(itemId.storeId.stuid.toString)
        colorField.setStringValue(colors.values.foldLeft("") { _ + " " + _ })
        sizeField.setStringValue(sizes.values.foldLeft("") { _ + " " + _ })
        brandField.setStringValue(brand.name)
        descriptionField.setReaderValue(new StringReader(description.text))
        productTitleField.setStringValue(productTitle.title)
        namedTypeField.setStringValue(namedType.name)

        val document = new Document()
        document.add(itemIdField)
        document.add(storeIdField)
        document.add(colorField)
        document.add(sizeField)
        document.add(brandField)
        document.add(clothingTypeField)
        document.add(descriptionField)
        document
    }

}