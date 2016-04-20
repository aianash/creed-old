package creed
package search

import akka.actor.{Props, Actor}

class SearchCache extends Actor {

  def receive = {
    case _ =>
  }

}

object SearchCache {
  def props = Props(classOf[SearchCache])
}