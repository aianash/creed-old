package creed
package query

// Inherit from CREED

sealed trait Intent {
  def value: String
  def intentType: String
  def copy(value: String): Intent
  override def toString = value
}

case class Activity(val value: String) extends Intent {
  val intentType = "activity"
  def copy(value: String) = Activity(value)
}

case class Look(val value: String) extends Intent {
  val intentType = "look"
  def copy(value: String) = Look(value)
}

case class TimeWeather(val value: String) extends Intent {
  val intentType = "timeWeather"
  def copy(value: String) = TimeWeather(value)
}

case class ALT(activity: Activity, look: Look, timeWeather: TimeWeather) {
  override def toString = s"${activity}:${look}:${timeWeather}"
  def str = activity.value + " " + look.value + " " + timeWeather.value
}

trait IntentBuildr[I <: Intent] {
  def build(i: String): I
}

object Intent {
  val ACTIVITYCLAZZ    = classOf[Activity]
  val LOOKCLAZZ        = classOf[Look]
  val TIMEWEATHERCLAZZ = classOf[TimeWeather]
}

trait IntentBuildrs {

  implicit object ActivityBuildr extends IntentBuildr[Activity] {
    def build(a: String) = Activity(a)
  }

  implicit object LookBuildr extends IntentBuildr[Look] {
    def build(l: String) = Look(l)
  }

  implicit object TimeWeatherBuildr extends IntentBuildr[TimeWeather] {
    def build(tw: String) = TimeWeather(tw)
  }
}