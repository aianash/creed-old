package creed
package query
package models
package alt

import scala.reflect._
import scala.collection.SortedSet
import scala.collection.mutable.{SortedSet => MSortedSet, HashMap => MHashMap}

import it.unimi.dsi.fastutil.objects.{Object2DoubleOpenHashMap, Object2IntOpenHashMap, ObjectArrayList}

import org.mapdb._

import hemingway.dictionary._, similarity.Jaccard


case class SimSet(id: String, contexts: SortedSet[(String, String)], intents: SortedSet[(String, Double)])

object SimSet {

  trait BuildrFor[T <: Occurrence[_]] { def buildr(id: String): Buildr[T] }
  implicit object BuildrForSingle  extends BuildrFor[Single]  { def buildr(id: String) = new SingleSimSetBuildr(id)  }
  implicit object BuildrForCooccur extends BuildrFor[Cooccur] { def buildr(id: String) = new CooccurSimSetBuildr(id) }

  def newBuildrFor[T <: Occurrence[_] : BuildrFor](id: String) = implicitly[BuildrFor[T]].buildr(id)

  trait Buildr[T <: Occurrence[_]] {
    def id: String
    def add(occur: T): Buildr[T]
    def infoGain(newoccur: T): Double
    def result: SimSet

    def +=(occur: T) = add(occur)
  }

  class SingleSimSetBuildr(val id: String) extends Buildr[Single] {

    private var single: Single = null

    def add(single: Single) = {
      this.single = single
      this
    }

    def result = SimSet(id, single.contexts, SortedSet(single.str -> single.relevance))

    def infoGain(newsingle: Single) = throw new UnsupportedOperationException("Why calling infoGain for singles")
  }

  class CooccurSimSetBuildr(val id: String) extends Buildr[Cooccur] {

    private val contexts = MSortedSet.empty[(String, String)]
    private val cooccurs = MSortedSet.empty[Cooccur](Ordering.by[Cooccur, (String, String)](x => x._1 -> x._2))
    private var entropy = 0.0 // base ln

    def add(cooccur: Cooccur) = {
      contexts ++= cooccur.contexts
      cooccurs += cooccur
      entropy = computeEntropy(contexts, cooccurs)
      this
    }

    def infoGain(newcooccur: Cooccur) =
      (entropy - computeEntropy(contexts ++ newcooccur.contexts, cooccurs + newcooccur))

    def result: SimSet = {
      val P = new Object2DoubleOpenHashMap[String]
      var Z = 0.0
      cooccurs foreach { cooccur =>
        val p = (cooccur.contexts.size * cooccur.relevance) / contexts.size
        P.addTo(cooccur._1, p)
        P.addTo(cooccur._2, p)
        Z += p
      }

      val buildr = SortedSet.newBuilder[(String, Double)]
      val iter = P.object2DoubleEntrySet.iterator()
      while(iter.hasNext) {
        val entry = iter.next
        val p = entry.getDoubleValue
        val intent = entry.getKey
        buildr += intent -> p
      }

      SimSet(id, contexts, buildr.result)
    }

    private def computeEntropy(contexts: MSortedSet[(String, String)], cooccurs: MSortedSet[Cooccur]): Double = {
      val P = new Object2DoubleOpenHashMap[String]
      cooccurs foreach { cooccur =>
        val p = (cooccur.contexts.size * cooccur.relevance) / contexts.size
        P.addTo(cooccur._1, p)
        P.addTo(cooccur._2, p)
      }
      var Z      = 0.0
      var Ep     = 0.0
      var EpLogp = 0.0
      val iter = P.values().iterator
      while(iter.hasNext) {
        val p = iter.nextDouble
        Z      += p
        Ep     += p
        EpLogp += p * math.log(p)
      }
      if(Z == 0.0) 0.0
      else {
        val EplogZ = math.log(Z) * Ep
        - 1.0 * (EpLogp - EplogZ) / Z
      }
    }
  }

}

case class ScoredSimSetIds[I <: Intent](simsets: IndexedSeq[(String, Double)]) {
  def ++(that: ScoredSimSetIds[I]): ScoredSimSetIds[I] = copy(simsets = simsets ++ that.simsets)
}

class SimSets(db: DB) {
  import SimSets.Namespace

  private val dictionary = FileBasedDictionary("simsets", db)

  private val contexts = db.treeSetCreate("_contexts")
                           .serializer(BTreeKeySerializer.ARRAY3)
                           .makeOrGet[Array[Object]]
  private val contexts_1 = db.treeSetCreate("_contexts_1")
                           .serializer(BTreeKeySerializer.ARRAY2)
                           .makeOrGet[Array[Object]]
  private val contexts_2 = db.treeSetCreate("_contexts_2")
                           .serializer(BTreeKeySerializer.ARRAY2)
                           .makeOrGet[Array[Object]]

  private val similarity = Jaccard(0.4)

  def simsets(target: Activity, context: (Look, TimeWeather)) = simsets[Activity](target.value, context._1.value -> context._2.value)
  def simsets(target: Look, context: (Activity, TimeWeather)) = simsets[Look](target.value, context._1.value -> context._2.value)
  def simsets(target: TimeWeather, context: (Activity, Look)) = simsets[TimeWeather](target.value, context._1.value -> context._2.value)

  private def simsets[I <: Intent : Namespace](str: String, context: (String, String)): ScoredSimSetIds[I] = {
    val namespace = implicitly[Namespace[I]]
    val similar: IndexedSeq[(String, Double)] =
      dictionary.findSimilar(str, similarity)
                .map { entry => entry.payload("id") -> entry.payload("rel").toDouble }
                .filter(namespace partOf _._1)
                .toIndexedSeq
    val result = similar.filter(x => contextsFor(x._1, has = context))
    if(result.isEmpty) ScoredSimSetIds[I](similar)
    else ScoredSimSetIds[I](result)
  }

  def simsetsAnyContext[I <: Intent : Namespace](str: String, context: (Seq[String], Seq[String])): ScoredSimSetIds[I] = {
    val namespace = implicitly[Namespace[I]]
    val similar: IndexedSeq[(String, Double)] =
      dictionary.findSimilar(str, similarity)
                .map { entry => entry.payload("id") -> entry.payload("rel").toDouble }
                .filter(namespace partOf _._1)
                .toIndexedSeq

    // SOME OPTIMIZATION IS REQUIRED HERE
    val result =
      if(context._1.isEmpty && context._2.isEmpty) similar
      else if(!context._1.isEmpty && !context._2.isEmpty) {
        val contexts = for(a <- context._1; b <- context._2) yield a -> b
        similar.filter { x => contexts.exists(context => contextsFor(x._1, has = context)) }
      } else if(context._1.isEmpty)
        similar.filter { x => context._2.exists(context => contextsFor(x._1, has = context, at = 2)) }
      else
        similar.filter { x => context._1.exists(context => contextsFor(x._1, has = context, at = 1)) }

      if(result.isEmpty) ScoredSimSetIds[I](similar)
      else ScoredSimSetIds[I](result)
  }

  def +=(simset: SimSet) = add(simset)

  def add(simset: SimSet) = {
    val payload = Map("id" -> simset.id)
    for((intent, relevance) <- simset.intents) dictionary += intent -> (payload + ("rel" -> relevance.toString))
    for((_1, _2) <- simset.contexts) {
      var arr = Array[Object](simset.id, _1, _2)
      contexts.add(arr)
      contexts_1.add(arr)
      arr(1) = _2
      contexts_2.add(arr)
    }
    this
  }

  @inline private def contextsFor(simsetId: String, has: (String, String)): Boolean =
    Fun.filter(contexts, simsetId, has._1, has._2).iterator.hasNext

  @inline private def contextsFor(simsetId: String, has: String, at: Int): Boolean = at match {
    case 1 => Fun.filter(contexts_1, simsetId, has).iterator.hasNext
    case 2 => Fun.filter(contexts_2, simsetId, has).iterator.hasNext
    case _ => false
  }


}

object SimSets {

  trait Namespace[I <: Intent] {
    def str: String
    def partOf(simsetId: String) = {
      val splits = simsetId.split('.')
      if(splits.size > 2 && splits(1) == str) true
      else false
    }
  }

  object Namespace {
    implicit object ActivityNamespace extends Namespace[Activity] { val str = "a" }
    implicit object LookNamespace extends Namespace[Look] { val str = "l" }
    implicit object TimeWeatherNamespace extends Namespace[TimeWeather] { val str = "tw" }
  }

  def newBuildr[I <: Intent : Namespace](errorConfidence: Double): Buildr[I] = new Buildr[I](errorConfidence)

  class Buildr[I <: Intent : Namespace](errorConfidence: Double) {
    private val namespace = implicitly[Namespace[I]].str

    private val cooccurSimSetBuildrs = new ObjectArrayList[SimSet.Buildr[Cooccur]]
    private val singleSimSetBuildrs  = new ObjectArrayList[SimSet.Buildr[Single]]
    private val nextId               = new Object2IntOpenHashMap[String]

    def add(single: Single): Unit = singleSimSetBuildrs.add(SimSet.newBuildrFor[Single](idFor(single)) += single)

    def add(cooccur: Cooccur, atleastIG: Double): Double =
      if(cooccurSimSetBuildrs.isEmpty) {                   // (A) For the first time create a new SimSet with this cooccur
        cooccurSimSetBuildrs.add(SimSet.newBuildrFor[Cooccur](idFor(cooccur)) += cooccur)
        atleastIG                                          //       carry on the Information Gain threshold
      } else {
        val itr = cooccurSimSetBuildrs.iterator
        var avgIG = 0.0
        val IGs = new Object2DoubleOpenHashMap[SimSet.Buildr[Cooccur]]
                                              // (B.1) calculate information gain with each existing simsets
        while(itr.hasNext) {
          val simset = itr.next
          val IG = simset.infoGain(cooccur)
          avgIG += IG
          IGs.put(simset, IG)
        }
        avgIG = avgIG / cooccurSimSetBuildrs.size

        var merged = false
        val threshold = atleastIG * errorConfidence
        var minIG = Double.MaxValue
        if(avgIG > threshold) {               // B.2.a Add to right simsets with the average Information Gain is greater than
                                              // the threshold
          val itr2 = IGs.object2DoubleEntrySet.iterator
          while(itr2.hasNext) {
            val entry  = itr2.next
            val IG     = entry.getDoubleValue
            val simset = entry.getKey
            if(IG >= avgIG) {                 // B.2.b add to simset if information gain is greater than average
              merged = true
              simset += cooccur
              if(IG < minIG) minIG = IG
            }
          }
        }

        if(merged) minIG                      // B.3 If cooccur was merged with atleast one simset,
                                              // then return the mininum infomation gain accepted
        else {                                // else
                                              // create a new simset and return the same atleastIG
          cooccurSimSetBuildrs.add(SimSet.newBuildrFor[Cooccur](idFor(cooccur)) += cooccur)
          atleastIG
        }
      }

    def saveTo(model: DB) {
      val simsets = new SimSets(model)
      val cooccurIter = cooccurSimSetBuildrs.iterator
      while(cooccurIter.hasNext) simsets += cooccurIter.next.result
    }

    private def idFor(single: Single) =
      single.str + "." + namespace + "." + nextId.addTo(single.str, 1)

    private def idFor(cooccur: Cooccur) =
      cooccur._1 + "." + namespace + "." + nextId.addTo(cooccur._1, 1)

  }

}