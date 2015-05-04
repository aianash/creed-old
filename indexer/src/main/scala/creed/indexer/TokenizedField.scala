package creed.indexer

import org.apache.lucene.document.Field
import org.apache.lucene.document.Field._
import org.apache.lucene.document.FieldType
import org.apache.lucene.index.FieldInfo.IndexOptions

class TokenizedField(name: String, value: String, stored: Store) extends Field(name, value, if(stored == Store.YES) TokenizedField.TYPE_STORED else TokenizedField.TYPE_NOT_STORED) {

  def setTokens(tokens: Seq[String]) {
    println("DEBUG: TYPE_NOT_STORED.stored() " + TokenizedField.TYPE_NOT_STORED.stored())
    println("DEBUG: TYPE_NOT_STORED.indexOptions()" + TokenizedField.TYPE_NOT_STORED.indexOptions())
    super.setTokenStream(new ArrayTokenStream(tokens))
  }

}

object TokenizedField {

  /**
   * Indexed, tokenized, not stored
   */
  val TYPE_NOT_STORED = new FieldType

  /**
   * Indexed, tokenized, stored
   */
  val TYPE_STORED = new FieldType

  TYPE_NOT_STORED.setOmitNorms(true)
  TYPE_NOT_STORED.setTokenized(true)
  TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_ONLY)
  TYPE_NOT_STORED.freeze

  TYPE_STORED.setOmitNorms(true)
  TYPE_STORED.setTokenized(true)
  TYPE_STORED.setIndexOptions(IndexOptions.DOCS_ONLY)
  TYPE_STORED.setStored(true)
  TYPE_STORED.freeze

}