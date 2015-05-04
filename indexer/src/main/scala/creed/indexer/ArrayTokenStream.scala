package creed.indexer

import java.io.IOException

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute


class ArrayTokenStream(values: Seq[String]) extends TokenStream {
  println("Inside CreedTokenStream::constructor. Values : " + values)
  private val tokens   = values
  private var index    = 0
  private val termAttr = addAttribute(classOf[CharTermAttribute])

  override def reset() {
    println("DEBUG: Inside CreedTokenStream::reset")
    index = 0
  }

  @throws(classOf[IOException])
  override def incrementToken = {
    println("DEBUG: Inside CreedTokenStream::incrementToken\n\tIndex : " + index)
    clearAttributes
    if(index >= tokens.length) false
    else {
      val token = tokens(index)
      // set term attribute
      termAttr.setEmpty
      termAttr.resizeBuffer(token.length)
      termAttr.append(token)
      index += 1
      true
    }
  }

}