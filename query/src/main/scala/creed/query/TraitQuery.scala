package creed
package query

import org.apache.lucene.index.{IndexReaderContext, LeafReader, LeafReaderContext, PostingsEnum, ReaderUtil}
import org.apache.lucene.index.{Term, TermContext, TermState, TermsEnum}
import org.apache.lucene.search.{IndexSearcher, Query, Weight, Scorer, DocIdSetIterator, Explanation}
import org.apache.lucene.search.similarities.Similarity
import org.apache.lucene.search.similarities.Similarity.SimScorer
import org.apache.lucene.util.{Bits, ToStringUtils}


class TraitQuery(itemTrait: Term, val searchContext: SearchContext) extends Query {
  require(itemTrait != null, new IllegalArgumentException("provided itemTrait cannot be null"))

  def term = itemTrait

  override def createWeight(searcher: IndexSearcher, needsScores: Boolean) = {
    val context = searcher.getTopReaderContext
    val termStates = TermContext.build(context, term)
    new TraitWeight(searcher, needsScores, termStates, searchContext)
  }

  override def toString(field: String) = {
    "TODO user readable version of this query"
  }

  override def equals(that: Any) = that match {
    case q: TraitQuery =>
      super.equals(that) && term.equals(q.term) && searchContext.equals(q.searchContext)
    case _ => false
  }

  override def hashCode = {
    super.hashCode ^ term.hashCode ^ searchContext.hashCode
  }

  class TraitWeight(searcher: IndexSearcher,
                    needsScores: Boolean,
                    val termStates: TermContext,
                    searchContext: SearchContext) extends Weight(TraitQuery.this) {

    val similarity = searcher.getSimilarity(needsScores)
    val stats = similarity.computeWeight(getBoost,
                  searcher.collectionStatistics(term.field),
                  searcher.termStatistics(term, termStates))

    override def extractTerms(terms: java.util.Set[Term]): Unit =
      terms.add(term)

    override def toString() = "weight(" + TraitQuery.this + ")";

    override def getValueForNormalization() = stats.getValueForNormalization

    override def normalize(queryNorm: Float, topLevelBoost: Float): Unit =
      stats.normalize(queryNorm, topLevelBoost)

    override def scorer(context: LeafReaderContext, acceptDocs: Bits) = {
      assert(termStates.topReaderContext == ReaderUtil.getTopLevelContext(context),
        "The top-reader used to create Weight (" + termStates.topReaderContext + ") is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context))
      val termsEnum = getTermsEnum(context)
      if(termsEnum == null) null
      else {
        val docs = termsEnum.postings(acceptDocs, null, if(needsScores) PostingsEnum.FREQS else PostingsEnum.NONE)
        assert(docs != null)
        new TraitScorer(this, term.text, docs, searchContext, similarity.simScorer(stats, context), searcher)
      }
    }

    def getTermsEnum(context: LeafReaderContext) = {
      val state = termStates.get(context.ord)
      if(state == null) {
        assert(context.reader.docFreq(term) == 0,
          "no termstate found but term exists in reader term=" + term)
        null
      } else {
        val termsEnum = context.reader.terms(term.field).iterator
        termsEnum.seekExact(term.bytes, state)
        termsEnum
      }
    }

    override def explain(context: LeafReaderContext, doc: Int) =
      Explanation.noMatch("not yet implemented")
  }

}

class TraitScorer(weight: Weight, itemTrait: String, postingsEnum: PostingsEnum, searchContext: SearchContext, docScorer: Similarity.SimScorer, searcher: IndexSearcher)
  extends Scorer(weight) {

  override def docID = postingsEnum.docID

  override def freq = postingsEnum.freq()

  override def nextDoc = postingsEnum.nextDoc

  override def score = {
    assert(docID != DocIdSetIterator.NO_MORE_DOCS)
    val itemType = searcher.doc(docID).get("itemType").toString
    searchContext.traitScoreFor(itemTrait, itemType)
  }

  override def advance(target: Int) = postingsEnum.advance(target)

  override def cost = postingsEnum.cost

  override def toString = "scorer(" + weight + ")[" + super.toString + "]"
}