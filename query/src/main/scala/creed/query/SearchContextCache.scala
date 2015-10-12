package creed
package query


import akka.actor.{Actor, Props}

class SearchContextCache extends Actor {

  def receive = {
    case _ =>
  }

}

object SearchContextCache {
  def props = Props(classOf[SearchContextCache])
}