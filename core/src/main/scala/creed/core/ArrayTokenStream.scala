package creed.core

import java.io.IOException

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute


class ArrayTokenStream(values: Seq[String]) extends TokenStream {
  private val tokens   = values
  private var index    = 0
  private val termAttr = addAttribute(classOf[CharTermAttribute])

  override def reset() {
    index = 0
  }

  @throws(classOf[IOException])
  override def incrementToken = {
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