package creed
package core
package cassie

import akka.actor.{Props, Actor}


/** Should be added as dependency from cassie-core
  *
  */
class CassieClient extends Actor {

  def receive = {
    case _ =>
  }
}

object CassieClient {
  def props = Props(classOf[CassieClient])
}

import goshoplane.commons.core.protocols.Replyable

case class GetCatalogueItems(itemIds: Seq[CatalogueItemId]) extends Replyable[Seq[CatalogueItem]]