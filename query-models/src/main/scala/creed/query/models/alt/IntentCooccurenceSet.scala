/**
 * KEEPING THIS CODE FOR A WHILE
 * EVEN IF ITS NOT USED YET
 * PROBABLY IT WILL BE

package creed
package query
package models
package alt

import scala.collection.JavaConversions._
import scala.collection.mutable.Builder
import scala.collection.SortedSet

import it.unimi.dsi.fastutil.objects.ObjectArrayList

case class Cooccur[+I <: Intent[I]](i1: I, i2: I, measure: Float)

class IntentCooccurenceSet[I <: Intent[I]](implicit buildr: IntentBuildr[I]) {

  val entries = new ObjectArrayList[Cooccur[I]]

  private val comparator = Ordering.by[Cooccur[I], (String, Float, String)](x => (x.i1.value, x.measure, x.i2.value))

  def +=(cooccur: Cooccur[I]): this.type = {
    var first = 0
    var last = entries.size - 1
    var i  = first + (last - first) / 2

    while(last >= first) {
      val entry = entries.get(i)
      val compare = comparator.compare(cooccur, entry)
      if(compare == 0) return this
      else if(compare > 0) first = i + 1
      else last = i - 1
      i = first + (last - first) / 2
    }

    if(i == entries.size) entries.add(cooccur)
    else entries.add(i, cooccur)
    this
  }

  def get(key: I): SortedSet[(I, Float)] = {
    var first = 0
    var last = entries.size - 1
    var i = first + (last - first) / 2

    val buildr = SortedSet.newBuilder[(I, Float)](Ordering.by[(I, Float), (Float, String)](x => x._2 -> x._1.value))

    while(last >= first) {
      val entry = entries.get(i)
      val compare = key.value compare entry.i1.value
      if(compare == 0) return withNearby(i, buildr)
      else if(compare > 0) first = i + 1
      else last = i - 1
      i = first + (last - first) / 2
    }

    return buildr.result()
  }

  def iterator: Iterator[Cooccur[I]] = entries.iterator

  private def withNearby(idx: Int, buildr: Builder[(I, Float), SortedSet[(I, Float)]]): SortedSet[(I, Float)] = {
    val entry  = entries.get(idx)
    val target = entry.i1.value

    buildr += entry.i2 -> entry.measure

    var i = idx - 1
    while(i >= 0 && entries.get(i).i1.value == target) {
      val entry = entries.get(i)
      buildr += entry.i2 -> entry.measure
      i -= 1
    }

    i = idx + 1
    while(i < entries.size && entries.get(i).i1.value == target) {
      val entry = entries.get(i)
      buildr += entry.i2 -> entry.measure
      i += 1
    }

    buildr.result()
  }

}

object IntentCooccurenceSet {

  def empty[I <: Intent[I] : IntentBuildr] = new IntentCooccurenceSet[I]

  def newBuildr[I <: Intent[I] : IntentBuildr] = new Buildr[I]

  class Buildr[I <: Intent[I] : IntentBuildr] {
    def +=(cooccur: Cooccur[I]) = this
    def result(): IntentCooccurenceSet[I] = new IntentCooccurenceSet[I]
  }

}
*/