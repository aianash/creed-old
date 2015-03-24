package creed.indexer

import scala.util.{Try, Success, Failure}

import akka.actor.Actor

import kafka.consumer._
import kafka.serializer.DefaultDecoder

import creed.core._

/**
 * Actor to get data from kafka in batch.
 */
class CatalogueItemConsumer(connector: ConsumerConnector) extends Actor {

  import protocols._

  val filterSpec = new Whitelist("test")
  val streams = connector.createMessageStreamsByFilter(filterSpec, 1, new DefaultDecoder(), new DefaultDecoder())
  val stream = streams(0)
  val iterator = stream.iterator()

  def receive = {
    case ReadNextCatalogueItem(batchSize) =>
      val replyTo = sender();

      getNextBatch(batchSize) match {
        case Success(batch) => replyTo ! NextBatch(batch = batch)
        case Failure(ex) => replyTo ! akka.actor.Status.Failure(ex)
      }
  }

  /**
   * Return next batch of indexing items from kafka
   * @param {Int} batchSize: Batch Size
   */
  private def getNextBatch(batchSize: Int) = {
    Try {
      (0 to batchSize).foldLeft (List.empty[CatalogueItem]) { (batch, _) =>

        Try {iterator.next().message}
          .map({serialized => Some(Catalogue.decode(serialized))})
          .recover {
            case _: ConsumerTimeoutException => None
          } match {
            case Success(None) => batch
            case Success(Some(catalogueItem)) => catalogueItem :: batch
            case Failure(ex) => throw ex
          }
      }
    }
  }

}