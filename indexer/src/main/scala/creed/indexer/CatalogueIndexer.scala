package creed.indexer

import org.apache.lucene.index._

import akka.actor.Actor

class CatalogueIndexer(writer: IndexWriter) extends Actor {

  val converter = new CatalogueItemToDocument

  def receive() = {
    case IndexCatalogue(catalogueItem) =>
      val catalogueDocument = converter.convert(catalogueItem)
      writer.addDocument(catalogueDocument)
    case _ =>
  }

}