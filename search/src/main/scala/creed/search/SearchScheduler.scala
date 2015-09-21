package creed
package search

import scala.concurrent.duration._

import core.search.SearchId

import akka.actor.{ActorLogging, Actor, Props, ActorRef, Terminated}
import akka.pattern.pipe

import org.apache.lucene.search.ScoreDoc

import scalaz._, Scalaz._


/** Creates and maintains search jobs
  * Handle more-results using previous jobs
  * [TO DO]
  * - handle repeat request for scheduled search
  */
class SearchScheduler(cassie: ActorRef) extends Actor with ActorLogging {

  import SearchScheduler.{ExecutorMeta, CleanUp}
  import protocols._
  import core.search.protocols._
  import context.dispatcher

  val settings = SearchSettings(context.system)

  val searchId2Executor = collection.mutable.HashMap.empty[SearchId, ActorRef]
  val executors = collection.mutable.HashMap.empty[ActorRef, ExecutorMeta]

  val cache       = context.actorOf(SearchCache.props, "cache")

  val searcher    = context.actorSelection("../searcher")
  val backchannel = context.actorSelection("../backchannel")

  context watch cassie

  context.system.scheduler.schedule(settings.SCHEDULER_CLEANUP_START_DELAY,
                                    settings.SCHEDULER_CLEANUP_INTERVAL,
                                    self,
                                    CleanUp)

  def receive = {

    case ScheduleSearchingFor(searchId, searchContext) =>
      run(SearchJob(
        searchId      = searchId,
        searchContext = searchContext,
        searcher      = searcher,
        cassie        = cassie))


    case CleanUp =>
      executors
        .filter(_._2.job.elapsedDuration > settings.SCHEDULER_MAX_WAIT_FOR_JOB)
        .foreach { case (executor, meta) =>
          log.error("Sending job termination for search id u{}-sr{}", meta.searchId.userId.uuid, meta.searchId.sruid)
          executor ! TerminateJobExecution(meta.searchId)
        }


    case Terminated(executor) =>
      executors.get(executor) match {
        case Some(meta) =>
          import meta._
          log.info("Search job for u{}-sr{} executed in {}",
                    searchId.userId.uuid,
                    searchId.sruid,
                    job.executionTime)
          executors -= executor
          searchId2Executor -= searchId

        case None =>
      }

  }

  /** Run a job
    * [NOTE] Right now just terminates and recreate another
    * job. can do better than this.
    */
  def run(job: SearchJob) = {
    job.onResult { backchannel ! SendSearchResultFor(job.searchId, _) }

    searchId2Executor.get(job.searchId) match {
      case Some(executor) => executor ! TerminateJobExecution(job.searchId)
      case None =>
    }
    val executor = context.actorOf(SearchJobExecutor.props(job), job.id)
    context watch executor
    executors += (executor -> ExecutorMeta(job.searchId, job))
    searchId2Executor += (job.searchId -> executor)
  }

}

object SearchScheduler {
  def props(cassie: ActorRef) = Props(classOf[SearchScheduler], cassie)

  case object CleanUp
  case class ExecutorMeta(searchId: SearchId, job: SearchJob)
}