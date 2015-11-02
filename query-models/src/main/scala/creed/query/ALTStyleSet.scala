package creed
package query

import java.io.File

import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, StringField, Field, DoubleField}
import org.apache.lucene.store.SimpleFSLockFactory

import commons.catalogue._, attributes._

/**
 * This is used to infer the relevance of a style to an ALT.
 * For this purpose, lucene is being used here.
 */
object ALTStyleSet {

  /**
   * Indexer class for ALTStyleSet
   *
   * @param {String} indexDir : Path to the index directory
   */
  class Indexer(indexDir: String) {

    val path           = (new File(indexDir)).toPath()
    val indexDirectory = FSDirectory.open(path)
    val config         = new IndexWriterConfig(new StandardAnalyzer)
    val writer         = new IndexWriter(indexDirectory, config)

    /**
     * Adds alt, style and corresponding relevance to the training data
     *
     * @param {ALT}           alt       : ALT
     * @param {ClothingStyle} style     : Clothing style
     * @param {Float}         relevance : Relevance of given clothing style to alt
     */
    def add(alt: ALT, style: ClothingStyle, relevance: Float) {
      writer.addDocument(createDocument(alt, style, relevance))
    }

    /**
     * Should be called after the training is done.
     */
    def done = writer.close()

    /**
     * Creates lucene document
     *
     * @param {ALT}           alt       : ALT
     * @param {ClothingStyle} style     : Clothing style
     * @param {Float}         relevance : Relevance of a given clothing style to alt
     *
     * @return {Document} Lucene document
     */
    def createDocument(alt: ALT, style: ClothingStyle, relevance: Float) = {
      val doc = new Document()

      val activityField    = new StringField("activity", alt.activity.value, Field.Store.YES)
      val lookField        = new StringField("look", alt.look.value, Field.Store.YES)
      val timeWeatherField = new StringField("timeWeather", alt.timeWeather.value, Field.Store.YES)
      val styleField       = new StringField("style", style.name, Field.Store.YES)
      val relevanceField   = new DoubleField("relevance", relevance, Field.Store.YES)

      doc.add(activityField)
      doc.add(lookField)
      doc.add(timeWeatherField)
      doc.add(styleField)
      doc.add(relevanceField)

      doc
    }

  }

  /**
   * Searcher class for ALTStyleSet
   *
   * @param {String} searchDir : Path to the index directory
   */
  class Searcher(searchDir: String) {

    val path           = (new File(searchDir)).toPath()
    val indexDirectory = FSDirectory.open(path, null)
    val indexSercher   = new IndexSearcher(DirectoryReader.open(indexDirectory))

    def search(alts: Seq[ALT]) = {

    }

    def buildQuery(alts: Seq[ALT]) = {

    }

  }

  def trainer(path: String) = new Indexer(path)

}