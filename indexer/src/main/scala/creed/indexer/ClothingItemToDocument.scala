package creed.indexer

import creed.core._

import org.apache.lucene.document._
import org.apache.lucene.document.Field._
import org.apache.lucene.analysis.Analyzer

import java.io.StringReader

import scala.collection.mutable.HashMap

import goshoplane.commons.catalogue._

/**
 * Class to get Lucene Document from Clothing Item
 * @param ClothingItem
 */
class ClothingItemToDocument {

  private val itemIdField         = new StringField(ClothingIndexFields.ItemId.name, "", Field.Store.YES)
  private val storeIdField        = new StringField(ClothingIndexFields.StoreId.name, "", Field.Store.YES)
  private val itemTypeGroupsField = new StringField(ClothingIndexFields.ItemTypeGroups.name, "", Field.Store.NO)
  private val colorField          = new TokenizedField(ClothingIndexFields.Color.name, "", Field.Store.NO)
  private val sizeField           = new TokenizedField(ClothingIndexFields.Size.name, "", Field.Store.NO)
  private val brandField          = new StringField(ClothingIndexFields.Brand.name, "", Field.Store.NO)
  private val productTitleField   = new StringField(ClothingIndexFields.ProductTitle.name, "", Field.Store.NO)
  private val namedTypeField      = new StringField(ClothingIndexFields.NamedType.name, "", Field.Store.NO)
  private val fabricField         = new StringField(ClothingIndexFields.Fabric.name, "", Field.Store.NO)
  private val fitField            = new StringField(ClothingIndexFields.Fit.name, "", Field.Store.NO)
  private val styleField          = new StringField(ClothingIndexFields.Style.name, "", Field.Store.NO)
  private val priceField          = new FloatField(ClothingIndexFields.Price.name, 0L, Field.Store.NO)
  private val descriptionField    = new TextField(ClothingIndexFields.Description.name, new StringReader(""))

  def apply(item: ClothingItem) = {
    println("DEBUG: Inside ClothingItemToDocument::apply")
    itemIdField.setStringValue(item.itemId.cuid.toString)
    storeIdField.setStringValue(item.itemId.storeId.stuid.toString)
    brandField.setStringValue(item.brand.name)
    descriptionField.setReaderValue(new StringReader(item.description.text))
    productTitleField.setStringValue(item.productTitle.title)
    namedTypeField.setStringValue(item.namedType.name)
    colorField.setTokens(item.colors.values)
    sizeField.setTokens(item.sizes.values)
    //itemTypeGroupsField.setTokenStream(new CreedTokenStream(item.itemTypeGroups.groups))
    fabricField.setStringValue(item.fabric.fabric)
    fitField.setStringValue(item.fit.fit)
    styleField.setStringValue(item.style.style)
    priceField.setFloatValue(item.price.value)

    val document = new Document()
    document.add(itemIdField)
    document.add(storeIdField)
    document.add(colorField)
    document.add(sizeField)
    document.add(brandField)
    document.add(productTitleField)
    document.add(namedTypeField)
    document.add(fabricField)
    document.add(fitField)
    document.add(styleField)
    document.add(priceField)
    document.add(descriptionField)
    document
  }


}