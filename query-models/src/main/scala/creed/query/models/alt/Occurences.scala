package creed
package query
package models
package alt

import scala.collection.JavaConversions._
import scala.collection.mutable.{SortedSet => MSortedSet}

import it.unimi.dsi.fastutil.objects.{Object2ObjectOpenHashMap, Object2ObjectRBTreeMap, ObjectArrayList, ObjectRBTreeSet}


sealed trait Occurrence[T <: Occurrence[T]] {
  def relevance: Double
  def contexts: MSortedSet[(String, String)]
  def add(context: (String, String), relevance: Double): T

  def +=(cr: ((String, String), Double)): T = add(cr._1, cr._2)
}

private[alt] case class Single(val str: String) extends Occurrence[Single] {

  private var _relevance: Double = 0.0

  val contexts = MSortedSet.empty[(String, String)]

  def relevance = _relevance

  def add(context: (String, String), relevance: Double) = {
    contexts += context
    _relevance += relevance
    this
  }

}

private[alt] class Cooccur private (val _1: String, val _2: String) extends Occurrence[Cooccur] {

  private var _relevance: Double = 0.0

  // (intent, intent, _1weight, _2weight) - what weightage this intent-intent pair contribute to the knowledge of
  // this _1 and _2 being related thru cooccurence
  val contexts = MSortedSet.empty[(String, String)] //(Ordering.by[(String, String, Double, Double), (String, String)](x => x._1 -> x._2))

  // Jensen-Shanon Divergence between distribution of
  // _1 and _2
  def relevance = _relevance

  def add(context: (String, String), relevance: Double) = {
    contexts += context
    _relevance += relevance
    this
  }

  def shares(cooccur: Cooccur) =
    _1 == cooccur._1 ||
    _2 == cooccur._2 ||
    _1 == cooccur._2 ||
    _2 == cooccur._1
}

object Cooccur {

  def apply(_1: String, _2: String) = {
    val _id = id(_1, _2)
    new Cooccur(_id._1, _id._2)
  }

  def id(_1: String, _2: String) = if(_1 <= _2) (_1, _2) else (_2, _1)

}

private[alt] class Occurrences {

  private val invertedIndex = new Object2ObjectOpenHashMap[(String, String), ObjectArrayList[(String, Double)]]
  private val _cooccurs     = new Object2ObjectRBTreeMap[(String, String), Cooccur]
  private val _singles      = new Object2ObjectRBTreeMap[String, Single]
  private val cooccuring    = new ObjectRBTreeSet[String]

  /** Description of function
    *
    * @param target - target intent
    * @return Return value - blah blah
    */
  def add(target: Intent[_], context: (Intent[_], Intent[_]), weight: Double): Unit =
    add(target.value, context._1.value -> context._2.value, weight)

  /** Description of function
    *
    * @param Parameter1 - blah blah
    * @return Return value - blah blah
    */
  def add(_1: String, context: (String, String), _1weight: Double) {
    var _2s = invertedIndex.get(context)                                       //
    if(_2s == null) {
      _2s = new ObjectArrayList[(String, Double)]
      invertedIndex.put(context, _2s)
      if(!cooccuring.contains(_1)) {
        val single = _singles.get(_1)
        if(single == null) _singles.put(_1, Single(_1) += context -> _1weight)
        else single += context -> _1weight
      }
    } else if(_2s.size == 1) {
      val (_2, _2weight) = _2s.get(0)
      val relevance = partialJS(_1weight, _2weight)
      val cooccurId = Cooccur.id(_1, _2)
      val cooccur = _cooccurs.get(cooccurId)
      if(cooccur != null) cooccur.add(context, relevance)
      else _cooccurs.put(cooccurId, Cooccur(_1, _2).add(context, relevance))
      cooccuring.add(_1)
      cooccuring.add(_2)
      _singles.remove(_1)
      _singles.remove(_2)
    } else {
      val _2iter = _2s.iterator
      while(_2iter.hasNext) {
        val (_2, _2weight) = _2iter.next
        val relevance = partialJS(_1weight, _2weight)
        val cooccurId = Cooccur.id(_1, _2)
        var cooccur = _cooccurs.get(cooccurId)
        if(cooccur != null) cooccur.add(context, relevance)
        else _cooccurs.put(cooccurId, Cooccur(_1, _2).add(context, relevance))
      }
      cooccuring.add(_1)
      _singles.remove(_1)
    }
    _2s.add(_1 -> _1weight)
  }

  def cooccurs: Iterator[Cooccur] = _cooccurs.values.iterator
  def singles: Iterator[Single]   = _singles.values.iterator

  // single/a step in Jenson-Shanon divergence
  @inline private def partialJS(_1weight: Double, _2weight: Double): Double = {
    val m = (_1weight + _2weight) / 2
    ((_1weight * math.log(_1weight) + _2weight * math.log(_2weight)) / 2 - m * math.log(m)) / math.log(2)
  }

}
