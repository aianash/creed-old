// package creed.core

// import org.apache.lucene.document.Field
// import org.apache.lucene.document.Field._
// import org.apache.lucene.document.FieldType
// import org.apache.lucene.index.FieldInfo.IndexOptions

// class TokenizedField(name: String, stored: Store) extends Field(name, if(stored == Store.YES) TokenizedField.TYPE_STORED else TokenizedField.TYPE_NOT_STORED) {

//   def setTokens(tokens: Seq[String]) {
//     super.setTokenStream(new ArrayTokenStream(tokens))
//   }

// }

// object TokenizedField {

//   /**
//    * Indexed, tokenized, not stored
//    */
//   val TYPE_NOT_STORED = new FieldType
//   TYPE_NOT_STORED.setOmitNorms(true)
//   TYPE_NOT_STORED.setTokenized(true)
//   TYPE_NOT_STORED.setIndexed(true)
//   TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_ONLY)
//   TYPE_NOT_STORED.freeze

//   /**
//    * Indexed, tokenized, stored
//    */
//   val TYPE_STORED = new FieldType
//   TYPE_STORED.setOmitNorms(true)
//   TYPE_STORED.setTokenized(true)
//   TYPE_STORED.setIndexed(true)
//   TYPE_STORED.setIndexOptions(IndexOptions.DOCS_ONLY)
//   TYPE_STORED.setStored(true)
//   TYPE_STORED.freeze

// }