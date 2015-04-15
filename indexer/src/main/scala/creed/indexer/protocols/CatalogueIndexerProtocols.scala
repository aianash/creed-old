package creed.indexer.protocols

import creed.core._
import creed.indexer._

import scala.collection.mutable.ListBuffer

import akka.actor.ActorRef

sealed trait CatalogueIndexerMessage extends Serializable

// messages from CatalogueIndexer
case class CatalogueIndexerCreated(indexer: ActorRef) extends CatalogueIndexerMessage
case class RequestsCatalogue(indexer: ActorRef) extends CatalogueIndexerMessage
case class IndexingDone(indexer: ActorRef) extends CatalogueIndexerMessage
case class ErrorInIndexing(catalogueItem: CatalogueItem)

// message from IndexingSupervisor
case class IndexCatalogue(job: IndexingJob) extends CatalogueIndexerMessage
case object AskForCatalogue extends CatalogueIndexerMessage
case object NoCatalogueToIndex extends CatalogueIndexerMessage