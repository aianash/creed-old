package creed
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

  val FETCH_QUERY_FILTER_TIMEOUT = Timeout(config.getInt("creed.search.query.timeouts.for-fetch-filter").milliseconds)

  val SEARCH_RESULT_PROCESSING_TIMEOUT = Timeout(config.getInt("creed.search.timeouts.for-processing-search-result").milliseconds)
  val SEARCH_RESULT_PAGE_SIZE = config.getInt("creed.search.search.result-page-size")

  val MAX_QUERY_PROCESSING_TIME = config.getInt("creed.search.query.timeouts.for-processing").milliseconds
  val MAX_FETCH_SEARCH_RESULT_TIME = config.getInt("creed.search.timeouts.for-fetching-search-result").milliseconds
  val FETCH_MORE_SEARCH_RESULT_TIMEOUT = Timeout(config.getInt("creed.search.timeouts-for.fetching-more-search-result").milliseconds)
  val FETCH_SEARCH_CONTEXT_TIMEOUT = Timeout(config.getInt("creed.search.timeouts.for-fetching-search-context").milliseconds)

  val SCHEDULER_CLEANUP_START_DELAY = config.getInt("creed.search.scheduler.cleanup.start-delay").milliseconds
  val SCHEDULER_CLEANUP_INTERVAL = config.getInt("creed.search.scheduler.cleanup.interval").milliseconds
  val SCHEDULER_MAX_WAIT_FOR_JOB = config.getInt("creed.search.scheduler.max-wait-for-job").milliseconds
}

object SearchSettings extends ExtensionId[SearchSettings] with ExtensionIdProvider {
  override def lookup = SearchSettings

  override def createExtension(system: ExtendedActorSystem) =
    new SearchSettings(system.settings.config)

  override def get(system: ActorSystem): SearchSettings = super.get(system)
}