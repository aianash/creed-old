package creed
package core
package nlp

import scala.collection.JavaConversions._
import scala.collection.mutable.{Seq => MSeq}

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline._


object NLP {

  import CoreAnnotations._

  private val props = {
    val props = new Properties
    props.put("annotators", "tokenize, ssplit, pos, lemma")
    props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz")
    props.put("ner.applyNumericClassifiers", "false")
    props
  }

  private val pipeline = new StanfordCoreNLP(props)

  def nouns(str: String): Seq[String] = {
    val annotation = new Annotation(str)
    pipeline.annotate(annotation)
    val tokens: java.util.List[CoreLabel] = annotation.get(classOf[TokensAnnotation])
    if(tokens == null || tokens.isEmpty) return Seq.empty[String]
    val nouns = MSeq.newBuilder[String]
    for(token <- tokens) {
      val posTag = token.get(classOf[PartOfSpeechAnnotation])
      val lemma = token.get(classOf[LemmaAnnotation])
      if(posTag == null || lemma == null) return Seq.empty[String]
      if(posTag == "NNP" || posTag == "NP") nouns += lemma
    }
    nouns.result
  }

  /**
   * Lemmatizes a string by first tokenizing it and then lemmatizing each token
   * depending on provided criteria
   *
   * @param  {String}      str             : String to lemmatize
   * @param  {Seq[String]} posToLemmatize  : Parts of speech to lemmatize
   * @param  {Boolean}     keepUnlemmatize : If unlemmatized tokens should be returned
   *
   * @return {Seq[String]} Sequence of lemmatized words
   */
  def lemmatize(str: String, posToLemmatize: Seq[String], keepUnlemmatize: Boolean): Seq[String] = {
    val annotation = new Annotation(str)
    pipeline.annotate(annotation)
    val tokens: java.util.List[CoreLabel] = annotation.get(classOf[TokensAnnotation])
    if(tokens == null || tokens.isEmpty) Seq.empty[String]
    val lemmas = MSeq.newBuilder[String]
    for(token <- tokens) {
      val posTag = token.get(classOf[PartOfSpeechAnnotation])
      val lemma = token.get(classOf[LemmaAnnotation])
      if(posTag == null || lemma == null) return Seq.empty[String]
      if(posToLemmatize.contains(posTag)) lemmas += lemma
      else if(keepUnlemmatize) lemmas += token.word
    }
    lemmas.result
  }

}