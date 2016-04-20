package creed
package core

import scala.concurrent.duration._


case class WaitFor(duration: FiniteDuration) {
  var startTime = 0L
  def start = {
    startTime = System.currentTimeMillis
    this
  }
  def expired = (System.currentTimeMillis - startTime).milliseconds > duration
}