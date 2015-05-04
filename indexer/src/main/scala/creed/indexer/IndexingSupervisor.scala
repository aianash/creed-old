package creed.indexer

import creed.core._

import akka.actor.{Actor, ActorRef, ActorLogging, Props, Terminated}
import akka.pattern.ask
import akka.util.Timeout

import java.io.File
import java.util.UUID

import scala.concurrent._, duration._
import scala.collection.mutable.{Queue, Map}
import scala.util.Sorting

import org.apache.lucene.index._
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper

import goshoplane.commons.catalogue._

/**
 * Indexing job ID
 */
case class IndexingJobId(id: String)

/**
 * Indexing job case class
 */
case class IndexingJob(jobId: IndexingJobId, catalogueItems: List[CatalogueItem])

/**
 * Indexing Supervisor
 */
class IndexingSupervisor(consumer: ActorRef) extends Actor with ActorLogging {

  import IndexingSupervisor._
  import protocols._
  import context.dispatcher

  val settings = OnyxSettings(context.system)

  var indexDir = FSDirectory.open(new File(settings.IndexDirectory), null)
  val config   = new IndexWriterConfig(Version.LUCENE_48, getPerItemAnalyzerWrapper)
  config.setMaxBufferedDocs(settings.MaxBufferedDocs)
  config.setRAMBufferSizeMB(settings.MaxRAMBufferSize)
  val writer   = new IndexWriter(indexDir, config)

  val batchSize = settings.IndexingBatchSize

  // list of indexers who are registrered
  var indexers = Map.empty[ActorRef, Any]

  // list of actors requesting for indexing job
  val requests            = Queue.empty[ActorRef]
  var jobSchedulingStatus = Future {true}

  // local queue of catalogue items to be indexed and number of
  // retries
  val retryQueue = Queue.empty[CatalogueItem]

  // instantiate the CatalogueIndexer
  (0 to settings.IndexerCount).foreach { _ =>
    val indexer = context.actorOf(CatalogueIndexer.props(writer, self))
    context.watch(indexer)
  }


  def receive = {
    case CatalogueIndexerCreated(indexer) =>
      context.watch(indexer)
      indexers += (indexer -> None)

    case RequestsCatalogue(indexer) =>
      requests += indexer
      if(jobSchedulingStatus.isCompleted)
        jobSchedulingStatus = scheduleNextJob

    case CheckAndScheduleJob =>
      if(jobSchedulingStatus.isCompleted)
        jobSchedulingStatus = scheduleNextJob

    case IndexingDone(indexer) => indexers(indexer) = None

    case ErrorInIndexing(item) => retryQueue += item

    case Terminated(indexer) =>
      indexers(indexer)  match {
        case IndexingJob(id, catalogueItems) =>
          catalogueItems foreach { retryQueue += _ }
      }

  }


  /**
   * Schedules new indexing job
   */
  private def scheduleNextJob: Future[Boolean] = {
    val catalogueBatchF = getCatalogueBatch map {
      case NextBatch(batch) => batch
    }
    catalogueBatchF flatMap { batch =>
      if(batch.isEmpty && !requests.isEmpty) {
        backOffScheduling
        Future {true}
      } else if(batch.isEmpty) Future {true}
      else {
        val sendTo = requests.dequeue
        val job = IndexingJob(IndexingJobId(UUID.randomUUID().toString), batch)
        indexers(sendTo) = job
        sendTo ! IndexCatalogue(job)
        scheduleNextJob
      }
    }
  }


  private var currentBackoffIter = 0;

  private def backOffScheduling = {
    if(currentBackoffIter > settings.IndexSchedulingBackoffLimit) currentBackoffIter = 0
    currentBackoffIter += 1
    val interval = (Math.pow(2, currentBackoffIter) - 1 ) / 2
    context.system.scheduler.scheduleOnce(interval milliseconds, self, CheckAndScheduleJob)
  }

  /**
   * Returns catalogue batch
   */
  private def getCatalogueBatch() = {
    if(!retryQueue.isEmpty) {
      val batch =
        (0 to Math.min(batchSize, retryQueue.length))
          .foldLeft (List.empty[CatalogueItem]) { (batch, _) =>
            retryQueue.dequeue() :: batch
          }
      Future.successful(batch)
    } else {
      implicit val timeout = Timeout(5 seconds)
      val catalogueItemsF = (consumer ? ReadNextCatalogueBatch(batchSize)).mapTo[NextBatch]
      catalogueItemsF
    }
  }

  /**
   * override postStop method
   * close the index writer
   */
  override def postStop() {
    writer.close()
  }

  /**
   * Function to get PerItemAnalyzerWrapper
   * Uses analyzer specified in ClothingIndexFields
   */
  private def getPerItemAnalyzerWrapper = {
    val analyzer = new StandardAnalyzer(Version.LUCENE_48)
    val perFieldAnalyzer: java.util.Map[String, Analyzer] = new java.util.HashMap[String, Analyzer]

    perFieldAnalyzer.put(ClothingIndexFields.ProductTitle.name, ClothingIndexFields.ProductTitle.analyzer.get)
    perFieldAnalyzer.put(ClothingIndexFields.Description.name, ClothingIndexFields.Description.analyzer.get)
    perFieldAnalyzer.put(ClothingIndexFields.Fabric.name, ClothingIndexFields.Fabric.analyzer.get)
    perFieldAnalyzer.put(ClothingIndexFields.Fit.name, ClothingIndexFields.Fit.analyzer.get)
    perFieldAnalyzer.put(ClothingIndexFields.Style.name, ClothingIndexFields.Style.analyzer.get)
    new PerFieldAnalyzerWrapper(analyzer, perFieldAnalyzer)
  }

}


object IndexingSupervisor {

  case object CheckAndScheduleJob

}