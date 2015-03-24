package creed.indexer

import kafka.consumer._

import java.util.Properties

import akka.actor.ActorSystem
import akka.actor.Props

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Main class to start indexing server.
 * It reads catalogue items in batch and passes it to
 * IndexingSupervisor for indexing.
 *
 * Config:
 *
 * onyx {
 *   catalogue {
 *     kafka {
 *       group-id = "console-consumer"
 *       zookeeper-connect = "localhost:2181"
 *       auto-offset-reset = "smallest"
 *       conumser-timeout-ms = "500"
 *     }
 *     "actorSystem" = "onyx"
 *   }
 * }
 */
object IndexingServer {

  import protocols._

  def main(args: Array[String]) {

    val config = ConfigFactory.load("onyx")
    val system = ActorSystem(config.getString("onyx.catalogue.actorSystem"), config)
    val settings = OnyxSettings(system)
    val connector = getConnector(settings)
    var consumer = system.actorOf(Props(classOf[CatalogueItemConsumer], connector))

    consumer ! ReadNextCatalogueItem(10)

    system.scheduler.schedule(0 milliseconds, 1 seconds, consumer, ReadNextCatalogueItem(10))
  }

  /**
   * Returns an instance of ConsumerConnector with settings as an
   * input parameter
   */
  def getConnector(settings: OnyxSettings): ConsumerConnector = {
    val props = new Properties()
    props.put("group.id", settings.GroupId)
    props.put("zookeeper.connect", settings.ZookeeperConnect)
    props.put("auto.offset.reset", settings.AutoOffsetReset)
    props.put("consumer.timeout.ms", settings.ConsumerTimoutMs)
    val config = new ConsumerConfig(props)
    val connector = Consumer.create(config)
    connector
  }
}