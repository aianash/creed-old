package creed
package search

import scala.collection.JavaConversions._
import scala.concurrent.duration._

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Props, Actor, ActorLogging, Terminated, ActorSelection}
import akka.serialization._

import scalaz._, Scalaz._

import client.search._
import core.search._, core.search.protocols._
import core.WaitFor

import commons.catalogue._, collection._


class SearchBackchannel extends Actor with ActorLogging {
  import SearchBackchannel._
  import protocols._
  import context.dispatcher

  val endpoints = new ConcurrentHashMap[(SearchId, Class[_]), ActorSelection]
  val timekeep = new ConcurrentHashMap[(SearchId, Class[_]), WaitFor]

  val settings = SearchSettings(context.system)

  context.system.scheduler.schedule(settings.BACKCHANNEL_CLEANUP_START_DELAY,
                                    settings.BACKCHANNEL_CLEANUP_INTERVAL,
                                    self,
                                    CleanUp)

  def receive = {

    case RegisterBackchannelFor(searchId, endpoint, clazz, waitFor) =>
      endpoints.put(searchId -> clazz, ActorSelection(endpoint, Iterable.empty))
      timekeep.put(searchId -> clazz, waitFor.start)


    case SendThruBackchannelFor(searchId, message) =>
      println(message)
      endpoints.get(searchId -> message.getClass) match {
        case null =>
        case endpoint => endpoint ! message
      }
      endpoints.remove(searchId -> message.getClass)

    case CleanUp =>
      val removals = timekeep.filter(_._2.expired).map(_._1)
      removals foreach { k =>
        endpoints remove k
        timekeep remove k
      }
  }

}

object SearchBackchannel {
  def props = Props(classOf[SearchBackchannel])
  case object CleanUp
}