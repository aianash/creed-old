package creed
package core
package search

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem}
import akka.util.Timeout

import com.typesafe.config.{Config, ConfigFactory}

class SearchSettings(cfg: Config) extends Extension {

  final val config: Config = {
    val config = cfg.withFallback(ConfigFactory.defaultReference)
    config.checkValid(ConfigFactory.defaultReference, "creed")
    config
  }

  val INDEX_DIR = config.getString("creed.search.index-dir")
  val INTENT_DATASET_FILE = config.getString("creed.search.query.intent-dataset-file")

  val SEARCH_RESULT_PROCESSING_TIMEOUT = Timeout(config.getInt("creed.search.timeouts.for-processing-search-result").milliseconds)
  val SEARCH_RESULT_PAGE_SIZE = config.getInt("creed.search.result-page-size")

  val FETCH_QUERY_FILTER_TIMEOUT = Timeout(config.getInt("creed.search.query.timeouts.for-fetch-filter").milliseconds)
  val MAX_QUERY_PROCESSING_TIME = config.getInt("creed.search.query.timeouts.for-processing").milliseconds
  val MAX_FETCH_SEARCH_RESULT_TIME = config.getInt("creed.search.timeouts.for-fetching-search-result").milliseconds
  val FETCH_MORE_SEARCH_RESULT_TIMEOUT = Timeout(config.getInt("creed.search.timeouts.for-fetching-more-search-result").milliseconds)
  val FETCH_SEARCH_CONTEXT_TIMEOUT = Timeout(config.getInt("creed.search.timeouts.for-fetching-search-context").milliseconds)
  val MAX_WAIT_FOR_QUERY_RECOMMENDATION = config.getInt("creed.search.query.wait-for-query-recommendation").milliseconds
  val MAX_WAIT_FOR_SEARCH_RESULT = config.getInt("creed.search.wait-for-search-result").milliseconds

  val BACKCHANNEL_CLEANUP_START_DELAY = config.getInt("creed.search.backchannel.cleanup.start-delay").milliseconds
  val BACKCHANNEL_CLEANUP_INTERVAL = config.getInt("creed.search.backchannel.cleanup.interval").milliseconds

  val SCHEDULER_CLEANUP_START_DELAY = config.getInt("creed.search.scheduler.cleanup.start-delay").milliseconds
  val SCHEDULER_CLEANUP_INTERVAL = config.getInt("creed.search.scheduler.cleanup.interval").milliseconds
  val SCHEDULER_MAX_WAIT_FOR_JOB = config.getInt("creed.search.scheduler.max-wait-for-job").milliseconds

  val SEARCH_CONTEXT_CLEANUP_DELAY = config.getInt("creed.search.query.search-context.cleanup.start-delay").milliseconds
  val SEARCH_CONTEXT_CLEANUP_INTERVAL = config.getInt("creed.search.query.search-context.cleanup.interval").milliseconds

  val WAIT_FOR_SEARCH_CONTEXT = config.getInt("creed.search.query.search-context.wait-for-search-context").milliseconds
}

object SearchSettings extends ExtensionId[SearchSettings] with ExtensionIdProvider {
  override def lookup = SearchSettings

  override def createExtension(system: ExtendedActorSystem) =
    new SearchSettings(system.settings.config)

  override def get(system: ActorSystem): SearchSettings = super.get(system)
}