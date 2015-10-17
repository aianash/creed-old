package creed
package query
package datasets

import scala.util.Random
import scala.collection.Set
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import java.io.File
import java.util.{Set => JavaSet}

import org.mapdb._

import hemingway.dictionary.FileBasedDictionary
import hemingway.dictionary.similarity._

import core._, utils.MapDBUtils


class IntentDataset(db: DB) {

  import IntentDataset._

  private val intentDict = FileBasedDictionary("intent", db)

  private val activities   = db.hashSetCreate("_activities").makeOrGet[String].asScala
  private val looks        = db.hashSetCreate("_looks").makeOrGet[String].asScala
  private val timeWeathers = db.hashSetCreate("_timeWeathers").makeOrGet[String].asScala

  private val altsFreq = db.treeSetCreate("_alts_frequency")
                       .serializer(MapDBUtils.ARRAY5)
                       .makeOrGet[Array[Object]]

  var similarity: Similarity = Cosine(0.3)

  def +=[T](intent: Intent[T]): Unit = add(intent)
  def +=(rel: (ALT, Float)) = add(rel)

  def add[T](intent: Intent[T]): Unit = intent match {
    case Activity(activity) =>
      intentDict += (activity -> Map("type" -> intent.intentType))
      activities.add(activity)
    case Look(look) =>
      intentDict += (look -> Map("type" -> intent.intentType))
      looks.add(look)
    case TimeWeather(timeWeather) =>
      intentDict += (timeWeather -> Map("type" -> intent.intentType))
      timeWeathers.add(timeWeather)
    case _ =>
  }

  def add(rel: (ALT, Float)): Unit = {
    val alt = rel._1
    val relevance: java.lang.Float = rel._2
    var count: java.lang.Integer = 1
    val entry = Array[Object](alt.activity.value, alt.look.value, alt.timeWeather.value, relevance, count)
    val existingItr = Fun.filter(altsFreq, alt.activity.value, alt.look.value, alt.timeWeather.value, relevance).iterator
    if(existingItr.hasNext) {
      val existing = existingItr.next
      altsFreq.remove(existing)
      val existingCnt = existing(4).asInstanceOf[java.lang.Integer]
      val newCnt: java.lang.Integer = existingCnt.intValue() + 1
      entry(4) = newCnt
    }
    altsFreq.add(entry)
  }

  def findSimilar[T](intent: Intent[T], topK: Int): Set[Intent[T]] =
    intentDict.findSimilar(intent.value, similarity, topK)
              .filter(_.payload("type") == intent.intentType)
              .map(_.str.map(intent.copy(_)))
              .flatten

  def altIterator: Iterator[(ALT, Float, Int)] = altsFreq.iterator.map { arr =>
    val alt =
      ALT(Activity(arr(0).asInstanceOf[String]),
          Look(arr(1).asInstanceOf[String]),
          TimeWeather(arr(2).asInstanceOf[String]))
    val relevance = arr(3).asInstanceOf[java.lang.Float].floatValue()
    val count = arr(4).asInstanceOf[java.lang.Integer].intValue()
    (alt, relevance, count)
  }

  def generateRandomALT: Iterator[ALT] =
    (WindowedRandomIterator(activities) <+> looks |+| timeWeathers)((x, t) => ALT(Activity(x._1), Look(x._2), TimeWeather(t)))

  def count[I <: Intent[I]](clazz: Class[I]): Int = {
    import Intent._
    clazz match {
      case ACTIVITYCLAZZ    => activities.size
      case LOOKCLAZZ        => looks.size
      case TIMEWEATHERCLAZZ => timeWeathers.size
      case ANYTHINGCLAZZ    => -1
    }
  }

  def count[I <: Intent[I] : ClassTag]: Int =
    count(implicitly[ClassTag[I]].runtimeClass.asInstanceOf[Class[I]]) // ugly
}

object IntentDataset {

  def apply(filePath: String): IntentDataset = apply(makeDB(filePath))
  def apply(db: DB) = new IntentDataset(db)

}