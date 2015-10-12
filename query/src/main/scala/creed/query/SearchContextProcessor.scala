package creed
package query

import akka.actor.{Actor, Props}


class SearchContextProcessor extends Actor {
  import protocols._, models._

  val model = new SearchContextModel

  def receive = {
    case ProcessForSearchContext(searchId, query, styles) =>
      model.searchContext(searchId, query, styles)
  }

}

object SearchContextProcessor {
  def props = Props(classOf[SearchContextProcessor])
}