package creed
package query

import scala.util.hashing.MurmurHash3._
import scala.reflect.ClassTag
import scala.collection.SortedSet
import scala.collection.JavaConversions._
import scala.collection.mutable.Builder

import java.util.{Set => JSet, NavigableSet => JNavigableSet}
import java.util.concurrent.ConcurrentHashMap

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap, ObjectRBTreeSet, ObjectArrayList}
import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.ints.IntArrayList

import org.mapdb._


trait ALTSimSet[Key, Entry, SS <: ALTSimSet[Key, Entry, SS]] { self: SS =>
  private val _entries = new ObjectRBTreeSet[(Entry, Float)]

  def entries: JSet[(Entry, Float)] = _entries
  def key: Key

  def +=(entry: Entry, p: Float): SS = {
    entries.add(entry -> p)
    this
  }

  def ++=(entries: JSet[(Entry, Float)]): SS = {
    entries.addAll(entries)
    this
  }

}

/** SimSet for a paricular AB and elements of type C
  *
  * @param Parameter1 - blah blah
  * @return Return value - blah blah
  */
class ALTSimSet2[K1 <: Intent[K1], K2 <: Intent[K2], E <: Intent[E]](val key: (K1, K2)) extends ALTSimSet[(K1, K2), E, ALTSimSet2[K1, K2, E]]

class ALTSimSet1[K <: Intent[K], E1 <: Intent[E1], E2 <: Intent[E2]](val key: K) extends ALTSimSet[K, (E1, E2), ALTSimSet1[K, E1, E2]]

import ALTSimSets._

class ALTSimSets private (
  activityCooccurs: IntentCooccurenceSet[Intent[Activity]],
  lookCooccurs: IntentCooccurenceSet[Intent[Look]],
  timeWeatherCooccurs: IntentCooccurenceSet[Intent[TimeWeather]],
  _ALts: Seq[ALt],
  _ATls: Seq[ATl],
  _LTas: Seq[LTa],
  _Alts: Seq[Alt],
  _Lats: Seq[Lat],
  _Tals: Seq[Tal]
) {

  import Intent._

  private val ALts = _ALts.foldLeft(new ConcurrentHashMap[Int, ALt]) { (map, ss) => map.put(hash(ss.key), ss); map }
  private val ATls = _ATls.foldLeft(new ConcurrentHashMap[Int, ATl]) { (map, ss) => map.put(hash(ss.key), ss); map }
  private val LTas = _LTas.foldLeft(new ConcurrentHashMap[Int, LTa]) { (map, ss) => map.put(hash(ss.key), ss); map }

  private val Alts = _Alts.foldLeft(new ConcurrentHashMap[Int, Alt]) { (map, ss) => map.put(hash(ss.key), ss); map }
  private val Lats = _Lats.foldLeft(new ConcurrentHashMap[Int, Lat]) { (map, ss) => map.put(hash(ss.key), ss); map }
  private val Tals = _Tals.foldLeft(new ConcurrentHashMap[Int, Tal]) { (map, ss) => map.put(hash(ss.key), ss); map }

  def +=(entry: (ALT, Float)) = {
    // val (alT, atL, ltA) = ALTSimSet2(alt)
    // val (aLT, lAT, tAL) = ALTSimSet1(alt)
    // !!(alTSS.get(alT.id)) { _ ++= alT }
    // !!(atLSS.get(atL.id)) { _ ++= atL }
    // !!(ltASS.get(ltA.id)) { _ ++= ltA }

    // !!(aLTSS.get(aLT.id)) { _ ++= aLT }
    // !!(lATSS.get(lAT.id)) { _ ++= lAT }
    // !!(tALSS.get(tAL.id)) { _ ++= tAL }

    // add to cooccurences here
  }

  def num[I <: Intent[I]](a: I, b: I) = 1
  def num[I <: Intent[I]](a: I) = 1

  /** Save this simset to db
    *
    * @param db - mapdb to save this simset into
    * @return Return value - blah blah
    */
  def save(db: DB) {
    val (dbActivityCooccurs, dbLookCooccurs, dbTimeWeatherCooccurs) = getCooccursFrom(db)
    for(c <- activityCooccurs.iterator)    addTo(dbActivityCooccurs, c)
    for(c <- lookCooccurs.iterator)        addTo(dbLookCooccurs, c)
    for(c <- timeWeatherCooccurs.iterator) addTo(dbTimeWeatherCooccurs, c)

    // [TO DO] save simsets
  }

  private def addTo[I <: Intent[I]](cooccurences: JNavigableSet[Array[Object]], cooccur: Cooccur[Intent[I]]) = {
    import cooccur._
    val entry = Array[Object](i1.value, i2.value, measure.asInstanceOf[java.lang.Float])
    cooccurences.add(entry)
    entry(0) = i2.value
    entry(1) = i1.value
    cooccurences.add(entry)
  }

  private def hash[K1 <: Intent[K1], K2 <: Intent[K2]](key: (K1, K2)) =
    finalizeHash(mixLast(mix(stringHash(this.getClass.getName), stringHash(key._1.value)), stringHash(key._2.value)), 2)

  private def hash[K <: Intent[K]](key: K) =
    finalizeHash(mixLast(stringHash(this.getClass.getName), stringHash(key.value)), 1)

}

object ALTSimSets {

  def empty = new ALTSimSets(
    IntentCooccurenceSet.empty,
    IntentCooccurenceSet.empty,
    IntentCooccurenceSet.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty)

  /** Load this simset from db
    *
    * @param Parameter1 - blah blah
    * @return Return value - blah blah
    */
  def load(db: DB): ALTSimSets = {
    val (dbActivityCooccurs, dbLookCooccurs, dbTimeWeatherCooccurs) = getCooccursFrom(db)
    val activityCooccurs    = dbActivityCooccurs    .iterator.foldLeft(IntentCooccurenceSet.newBuildr[Intent[Activity]])    { _ += makeCooccurEntry[Intent[Activity]](_) } result()
    val lookCooccurs        = dbLookCooccurs        .iterator.foldLeft(IntentCooccurenceSet.newBuildr[Intent[Look]])        { _ += makeCooccurEntry[Intent[Look]](_) } result()
    val timeWeatherCooccurs = dbTimeWeatherCooccurs .iterator.foldLeft(IntentCooccurenceSet.newBuildr[Intent[TimeWeather]]) { _ += makeCooccurEntry[Intent[TimeWeather]](_) } result()

    new ALTSimSets(activityCooccurs, lookCooccurs, timeWeatherCooccurs, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty, Seq.empty)
  }

  private def makeCooccurEntry[I <: Intent[I] : IntentBuildr](arr: Array[Object]): Cooccur[I] = {
    val buildr = implicitly[IntentBuildr[I]]
    val i1 = buildr.build(arr(0).asInstanceOf[String])
    val i2 = buildr.build(arr(1).asInstanceOf[String])
    val p  = arr(2).asInstanceOf[Float]
    Cooccur(i1, i2, p)
  }

  private def getCooccursFrom(db: DB) = {
    val activityCooccurs    = db.treeSetCreate("_activities_cooccurences") .serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]
    val lookCooccurs        = db.treeSetCreate("_looks_cooccurences")      .serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]
    val timeWeatherCooccurs = db.treeSetCreate("_timeweather_cooccurences").serializer(BTreeKeySerializer.ARRAY3).makeOrGet[Array[Object]]
    (activityCooccurs, lookCooccurs, timeWeatherCooccurs)
  }

  class ALt(alt: ALT) extends ALTSimSet2[Intent[Activity], Intent[Look],        Intent[TimeWeather]](alt.activity -> alt.look)
  class ATl(alt: ALT) extends ALTSimSet2[Intent[Activity], Intent[TimeWeather], Intent[Look]](alt.activity -> alt.timeWeather)
  class LTa(alt: ALT) extends ALTSimSet2[Intent[Look],     Intent[TimeWeather], Intent[Activity]](alt.look -> alt.timeWeather)

  class Alt(alt: ALT) extends ALTSimSet1[Intent[Activity],    Intent[Look],     Intent[TimeWeather]](alt.activity)
  class Lat(alt: ALT) extends ALTSimSet1[Intent[Look],        Intent[Activity], Intent[TimeWeather]](alt.look)
  class Tal(alt: ALT) extends ALTSimSet1[Intent[TimeWeather], Intent[Activity], Intent[Look]](alt.timeWeather)

}

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
      if(compare == 0) return putNearby(i, buildr)
      else if(compare > 0) first = i + 1
      else last = i - 1
      i = first + (last - first) / 2
    }

    return buildr.result()
  }

  def iterator: Iterator[Cooccur[I]] = entries.iterator

  private def putNearby(idx: Int, buildr: Builder[(I, Float), SortedSet[(I, Float)]]): SortedSet[(I, Float)] = {
    val entry  = entries.get(idx)
    val target = entry.i1.value
    var i      = idx - 1

    buildr += entry.i2 -> entry.measure

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

// class IntentCooccurenceSet[I <: Intent[I]](implicit buildr: IntentBuildr[I]) {

//   val i1s      = new ObjectArrayList[String]
//   val skips    = new IntArrayList
//   val sizes    = new IntArrayList
//   val i2s      = new ObjectArrayList[String]
//   val measures = new FloatArrayList

//   def +=(cooccur: Cooccur[I]) = {
//     import cooccur._

//     var i1idx = indexOf(i1.value)
//     var skip  = skips.getInt(i1idx)
//     val size  = sizes.getInt(i1idx)

//     if(size != 0) {
//       val finish = skip + size
//       while(skip < finish && measures.getFloat(skip) >= measure) skip += 1
//     }

//     if(skip == i2s.size) { // append if its at the end
//       i2s.add(i2.value)
//       measures.add(measure)
//     } else {              // add if in the middle
//       i2s.add(skip, i2.value)
//       measures.add(skip, measure)
//     }

//     // move all skips after this i1 by 1
//     skip = i1idx + 1
//     while(skip < skips.size) {
//       skips.set(skip, skips.getInt(skip) + 1)
//       skip += 1
//     }

//     // increment the current size
//     sizes.set(i1idx, sizes.get(i1idx) + 1)
//     this
//   }

//   def get(i1: I): SortedSet[(I, Float)] = {
//     implicit val ordering = Ordering.by[(I, Float), (Float, String)](x => x._2 -> x._1.value)
//     var i1idx = indexOf(i1.value, false)
//     if(i1idx == -1) return SortedSet.empty
//     val res = SortedSet.newBuilder[(I, Float)]
//     val last = i1idx + sizes.getInt(i1idx)
//     while(i1idx <= last) {
//       res += buildr.build(i2s.get(i1idx)) -> measures.getFloat(i1idx)
//       i1idx += 1
//     }

//     res.result()
//   }

//   def iterator: Iterator[Cooccur[I]] = Iterator.empty

//   // binary searcha and get the index,
//   // if not present then add i1 and get the neqly created
//   // also add skips and sizes entry (corresponding)
//   private def indexOf(i1: String, addIfAbsent: Boolean = true): Int = {
//     var first = 0
//     var last  = i1s.size - 1
//     var i     = first + (last - first) /  2

//     while(last >= first) {
//       val compare = i1.compare(i1s.get(i))
//       if(compare == 0) return i
//       else if(compare > 0) first = i + 1
//       else last = i - 1
//       i = first + (last - first) / 2
//     }

//     if(!addIfAbsent) return -1

//     if(i == i1s.size) { // append if last
//       i1s.add(i1)
//       skips.add(i2s.size)
//       sizes.add(0)
//     } else {           // add if in between
//       i1s.add(i, i1)
//       if(skips.isEmpty) skips.add(i, 0)
//       else skips.add(i, skips.getInt(i))
//       sizes.add(i, 0)
//     }
//     i
//   }

// }

object IntentCooccurenceSet {

  def empty[I <: Intent[I] : IntentBuildr] = new IntentCooccurenceSet[I]

  def newBuildr[I <: Intent[I] : IntentBuildr] = new Buildr[I]

  class Buildr[I <: Intent[I] : IntentBuildr] {
    def +=(cooccur: Cooccur[I]) = this
    def result(): IntentCooccurenceSet[I] = new IntentCooccurenceSet[I]
  }

}