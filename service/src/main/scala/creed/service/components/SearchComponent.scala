package creed
package service
package components

import akka.actor.ActorSystem

import commons.microservice.Component

import creed.search._

case object SearchComponent extends Component {
  val name = "creed-search-service"
  val runOnRole = "creed-search"
  def start(system: ActorSystem) = system.actorOf(SearchService.props, name)
}
