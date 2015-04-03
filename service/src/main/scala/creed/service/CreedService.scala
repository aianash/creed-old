package creed.service

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod
import com.twitter.finagle.Thrift

import akka.actor.ActorSystem

import com.goshoplane.creed.search._
import com.goshoplane.creed.service._

import scaldi.Injector
import scaldi.akka.AkkaInjectable._


class CreedService(implicit inj: Injector) extends Creed[TwitterFuture] {
  def searchCatalogue(searchRequest: CatalogueSearchRequest) = {

    TwitterFuture.value(CatalogueSearchResults(searchRequest.searchId.userId))
  }
}

object CreedService {
  def start(implicit inj: Injector) = {
    val settings = CreedSettings(inject [ActorSystem])
    Thrift.serveIface(settings.CreedEndpoint, inject [CreedService])
  }
}