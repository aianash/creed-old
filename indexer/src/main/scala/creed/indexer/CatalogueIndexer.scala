package creed.indexer

import org.apache.lucene.index._

import creed.core._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.mutable.ListBuffer

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

import goshoplane.commons.catalogue._

/**
 * CatalogueIndexer is indexing worker that takes work from
 * IndexingSupervisor and informs it after the work is complete
 */
class CatalogueIndexer(writer: IndexWriter, supervisor: ActorRef) extends Actor {
  import protocols._
  import context.dispatcher

  val converter = new CatalogueItemToDocument

  /**
   * inform supervisor that CatalogueIndexer has been created
   */
  override def preStart() {
    supervisor ! CatalogueIndexerCreated(self)
    context.system.scheduler.scheduleOnce(500 milliseconds, supervisor, RequestsCatalogue(self))
  }

  /**
   * Message to let itself know that indexing is done
   */
  case object IndexingComplete

  /**
   * Function to index the CatalogueItem
   */
  def indexCatalogue(catalogueItems: List[CatalogueItem]) {
    Future {
      catalogueItems foreach { catalogueItem =>
        Try {
          val catalogueDocument = converter.convert(catalogueItem)
          writer.addDocument(catalogueDocument)
        } match {
          case Failure(ex) => supervisor ! ErrorInIndexing(catalogueItem)
          case _ =>
        }
      }
      self ! IndexingComplete
    }
  }

  /**
   * This is a state when CatalogueIndexer is in working state
   */
  def working: Receive = {
    case AskForCatalogue =>
    case NoCatalogueToIndex =>
    case IndexCatalogue(IndexingJob(jobId, catalogueItems)) =>
    case IndexingComplete =>
      supervisor ! IndexingDone(self)
      supervisor ! RequestsCatalogue(self)
      context.become(idle)
  }

  /**
   * This is a state when CatalogueIndexer is idle
   */
  def idle: Receive = {
    case AskForCatalogue => supervisor ! RequestsCatalogue(self)
    case NoCatalogueToIndex =>
    case IndexCatalogue(IndexingJob(jobId, catalogueItems)) =>
      context.become(working)
      indexCatalogue(catalogueItems)
  }

  /**
   * CatalogueIndexer is idle in the beginning
   */
  def receive = idle

}

/**
 * Companion object to instantiate CatalogueIndexer
 */
object CatalogueIndexer {

  def props(writer: IndexWriter, supervisor: ActorRef): Props = Props(classOf[CatalogueIndexer], writer, supervisor)

}