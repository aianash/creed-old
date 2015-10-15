package creed
package query

// Inherit from CREED

sealed trait Intent[+I] {
  def value: String
  def intentType: String
  def copy(value: String): Intent[I]
  override def toString = value
}

object Intent {
  val ACTIVITYCLAZZ = classOf[Activity]
  val LOOKCLAZZ = classOf[Look]
  val TIMEWEATHERCLAZZ = classOf[TimeWeather]
  val ANYTHINGCLAZZ = Anything.getClass
}

class Activity(val value: String) extends Intent[Activity] {
  val intentType = "activity"
  def copy(value: String) = Activity(value)
}

object Activity {
  def apply(value: String) = value match {
    case "*" => Anything
    case _ => new Activity(value)
  }

  def unapply(act: Activity) = Option(act.value)
}

class Look(val value: String) extends Intent[Look] {
  val intentType = "look"
  def copy(value: String) = Look(value)
}

object Look {
  def apply(value: String) = value match {
    case "*" => Anything
    case _ => new Look(value)
  }

  def unapply(look: Look) = Option(look.value)
}

class TimeWeather(val value: String) extends Intent[TimeWeather] {
  val intentType = "timeWeather"
  def copy(value: String) = TimeWeather(value)
}

object TimeWeather {
  def apply(value: String) = value match {
    case "*" => Anything
    case _ => new TimeWeather(value)
  }

  def unapply(tw: TimeWeather) = Option(tw.value)
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