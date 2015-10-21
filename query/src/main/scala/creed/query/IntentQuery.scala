package creed
package query

import org.apache.lucene.search._
import org.apache.lucene.index._
import org.apache.lucene.queries._
import org.apache.lucene.queries.CustomScoreQuery
import org.apache.lucene.queries.function.FunctionQuery


object IntentQuery {

  def build(searchContext: SearchContext) = {
    val traitQuery = {
      val query = searchContext.itemTraits.foldLeft(new BooleanQuery) { (query, itemTrait) =>
        val traitQ = new TraitQuery(new Term(itemTrait), searchContext)
        query.add(traitQ, BooleanClause.Occur.SHOULD)
        query
      }

      searchContext.itemTypes.foldLeft(query) { (query, itemType) =>
        query.add(new TermQuery(new Term(itemType)), BooleanClause.Occur.FILTER)
        query
      }
    }

    new IntentScoreQuery(searchContext, traitQuery)
  }

  // private val NORM_TABLE = Array.ofDim[Float](256)

  class IntentScoreQuery(searchContext: SearchContext, query: Query) extends CustomScoreQuery(query) {

    override def getCustomScoreProvider(context: LeafReaderContext) = {

      new CustomScoreProvider(context) {
        // val cache = Array.ofDim[Float](256)
        // for(i <- -1 until cache.length) {
        //   cache(i) = ((1 - b) + b * )
        // }
        override def customScore(doc: Int, subQueryScore: Float, valSrcScore: Float) = {
          // val freqVector = context.reader.getTermFreqVector(doc, "itemTraits")
          // val freq = freqVector.getTermFrequencies
          // val sum = 
          val itemType = context.reader.document(doc).get("itemType").toString
          subQueryScore + valSrcScore * (searchContext.ALTItemTypeScoreFor(itemType))
        }
      }
    }

  }




}

// package creed
// package query

// import java.util.{ArrayList, List}

// import org.apache.lucene.index.{IndexReaderContext, LeafReader, LeafReaderContext, PostingsEnum, ReaderUtil}
// import org.apache.lucene.index.{Term, TermContext, TermState, TermsEnum}
// import org.apache.lucene.search.{Query, BooleanQuery, BooleanClause, TermQuery, TwoPhaseIterator}
// import org.apache.lucene.search.{IndexSearcher, Query, Weight, Scorer, DocIdSetIterator, Explanation, ConjunctionDISI}
// import org.apache.lucene.search.similarities.Similarity
// import org.apache.lucene.search.similarities.Similarity.SimScorer
// import org.apache.lucene.util.{Bits, ToStringUtils}


// class IntentQuery(val searchContext: SearchContext) extends Query {

//   // think of moving whole of this to a single TraitQuery
//   val traitQuery = {
//     val query = searchContext.itemTraits.foldLeft(new BooleanQuery) { (query, itemTrait) =>
//       val traitQ = new TraitQuery(new Term(itemTrait), searchContext)
//       query.add(traitQ, BooleanClause.Occur.SHOULD)
//       query
//     }

//     searchContext.itemTypes.foldLeft(query) { (query, itemType) =>
//       query.add(new TermQuery(new Term(itemType)), BooleanClause.Occur.FILTER)
//       query
//     }
//   }

//   override def createWeight(searcher: IndexSearcher, needsScores: Boolean) = {
//     new IntentWeight(this, searcher, needsScores)
//   }

//   override def toString(field: String) = "to do"
// }


// class IntentWeight(query: IntentQuery, searcher: IndexSearcher, needsScores: Boolean) extends Weight(query) {

//   val traitWeight = query.traitQuery.createWeight(searcher, needsScores)

//   override def getValueForNormalization = traitWeight.getValueForNormalization

//   override def normalize(norm: Float, topLevelBoost: Float): Unit =
//     traitWeight.normalize(norm, topLevelBoost)

//   override def scorer(context: LeafReaderContext, acceptDocs: Bits) =
//     new IntentScorer(this, searcher, query.searchContext, traitWeight.scorer(context, acceptDocs))

//   override def extractTerms(terms: java.util.Set[Term]): Unit =
//     traitWeight.extractTerms(terms)

//   override def explain(context: LeafReaderContext, doc: Int) = Explanation.noMatch("not yet implemented")

// }


// class IntentScorer(weight: Weight, searcher: IndexSearcher, searchContext: SearchContext, traitScorer: Scorer)
//   extends Scorer(weight) {

//   override def asTwoPhaseIterator() = traitScorer.asTwoPhaseIterator

//   override def advance(target: Int) = traitScorer.advance(target)

//   override def docID = traitScorer.docID

//   override def freq = traitScorer.freq

//   override def nextDoc = traitScorer.nextDoc

//   override def score = {
//     assert(docID != DocIdSetIterator.NO_MORE_DOCS)
//     val itemType = searcher.doc(docID).get("itemType").toString
//     traitScorer.score + searchContext.ALTItemTypeScoreFor(itemType) *  1.0f
//   }

//   override def cost = traitScorer.cost

//   override def getChildren = {
//     val children = new java.util.ArrayList[Scorer.ChildScorer]()
//     children.add(new Scorer.ChildScorer(traitScorer, "MUST"))
//     children
//   }

// }