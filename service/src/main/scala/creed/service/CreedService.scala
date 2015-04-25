package creed.service

import scala.util.{Failure => TFailure, Success => TSuccess, Try}
import scala.util.control.NonFatal
import scala.concurrent._, duration._

import com.twitter.util.{Future => TwitterFuture}
import com.twitter.bijection._, twitter_util.UtilBijections._
import com.twitter.bijection.Conversion.asMethod
import com.twitter.finagle.Thrift
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec

import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import akka.pattern.ask

import com.goshoplane.creed.search._
import com.goshoplane.creed.service._

import scaldi.Injector
import scaldi.akka.AkkaInjectable._

import org.apache.thrift.protocol.TBinaryProtocol

import java.net.InetSocketAddress


class CreedService(implicit inj: Injector) extends Creed[TwitterFuture] {

  import protocols._

  val system     = inject [ActorSystem]
  val supervisor = system.actorOf(SearchingSupervisor.props)

  implicit val defaultTimeout = Timeout(1 seconds)

  def searchCatalogue(searchRequest: CatalogueSearchRequest) = {
    val searchResultsF = (supervisor ? SearchCatalogue(searchRequest)).mapTo[CatalogueSearchResults]
    awaitResult(searchResultsF, 500 milliseconds, {
      case NonFatal(ex) =>
        println(ex.getStackTrace)
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

    val protocol = new TBinaryProtocol.Factory()
    val service  = new Creed$FinagleService(inject [CreedService], protocol)
    val address  = new InetSocketAddress(settings.CreedPort)

    ServerBuilder()
      .codec(ThriftServerFramedCodec())
      .name(settings.ServiceName)
      .bindTo(address)
      .build(service)
  }
}