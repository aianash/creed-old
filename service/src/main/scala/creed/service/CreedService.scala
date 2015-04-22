package creed.service

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.util.control.NonFatal
import scala.concurrent._, duration._

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod
import com.twitter.finagle.Thrift

import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import akka.pattern.ask

import com.goshoplane.creed.search._
import com.goshoplane.creed.service._

import scaldi.Injector
import scaldi.akka.AkkaInjectable._


class CreedService(implicit inj: Injector) extends Creed[TwitterFuture] {

  import protocols._

  val system     = inject [ActorSystem]
  val supervisor = system.actorOf(SearchingSupervisor.props)

  implicit val defaultTimeout = Timeout(1 seconds)

  def searchCatalogue(searchRequest: CatalogueSearchRequest) = {
    val searchResultsF = (supervisor ? SearchCatalogue(searchRequest)).mapTo[CatalogueSearchResults]
    awaitResult(searchResultsF, 500 milliseconds, {
      case NonFatal(ex) =>
        TFailure(CreedException("Error while getting search results"))
    })
  }

  private def awaitResult[T, U >: T](future: Future[T], timeout: Duration, ex: PartialFunction[Throwable, Try[U]]): TwitterFuture[U] = {
    TwitterFuture.value(Try {
      Await.result(future, timeout)
    } recoverWith(ex) get)
  }

}


object CreedService {
  def start(implicit inj: Injector) = {
    val settings = CreedSettings(inject [ActorSystem])
    Thrift.serveIface(settings.CreedEndpoint, inject [CreedService])
  }
}