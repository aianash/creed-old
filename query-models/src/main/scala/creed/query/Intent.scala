package creed
package query

// Inherit from CREED

sealed trait Intent[+I] {
  def value: String
  def intentType: String
  def copy(value: String): Intent[I]
  override def toString = value
}

class Activity(val value: String) extends Intent[Activity] {
  val intentType = "activity"
  def copy(value: String) = Activity(value)
}

class Look(val value: String) extends Intent[Look] {
  val intentType = "look"
  def copy(value: String) = Look(value)
}

class TimeWeather(val value: String) extends Intent[TimeWeather] {
  val intentType = "timeWeather"
  def copy(value: String) = TimeWeather(value)
}

case object Anything extends Intent[Nothing] {
  val value = "*"
  val intentType = "anything"
  def copy(value: String) = this
}

case class ALT(activity: Intent[Activity], look: Intent[Look], timeWeather: Intent[TimeWeather]) {
  override def toString = s"${activity}:${look}:${timeWeather}"
  def str = activity.value + " " + look.value + " " + timeWeather.value
}

trait IntentBuildr[I <: Intent[I]] {
  def build(i: String): I
}

object Intent {
  val ACTIVITYCLAZZ = classOf[Activity]
  val LOOKCLAZZ = classOf[Look]
  val TIMEWEATHERCLAZZ = classOf[TimeWeather]
  val ANYTHINGCLAZZ = Anything.getClass
}

object Activity {
  def apply(value: String) = value match {
    case "*" => Anything
    case _   => new Activity(value)
  }

  def unapply(act: Activity) = Option(act.value)
}

object Look {
  def apply(value: String): Intent[Look] = value match {
    case "*" => Anything
    case _   => new Look(value)
  }

  def unapply(look: Look) = Option(look.value)
}

object TimeWeather {
  def apply(value: String) = value match {
    case "*" => Anything
    case _   => new TimeWeather(value)
  }

  def unapply(tw: TimeWeather) = Option(tw.value)
}


trait IntentBuildrs {
  implicit object ActivityBuildr extends IntentBuildr[Intent[Activity]] {
    def build(a: String) = Activity(a)
  }

  implicit object LookBuildr extends IntentBuildr[Intent[Look]] {
    def build(l: String) = Look(l)
  }

  implicit object TimeWeatherBuildr extends IntentBuildr[Intent[TimeWeather]] {
    def build(tw: String) = TimeWeather(tw)
  }
}