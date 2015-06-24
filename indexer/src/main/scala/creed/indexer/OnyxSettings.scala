package creed.indexer

import akka.actor.{ActorSystem, Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem}

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Onyx specific settings
 */
class OnyxSettings(cfg: Config) extends Extension {

  // validate Onyx config
  final val config: Config = {
    val config = cfg.withFallback(ConfigFactory.defaultReference)
    config.checkValid(ConfigFactory.defaultReference, "onyx")
    config
  }

  val GroupId                     = config.getString("onyx.catalogue.kafka.group-id")
  val ZookeeperConnect            = config.getString("onyx.catalogue.kafka.zookeeper-connect")
  val AutoOffsetReset             = config.getString("onyx.catalogue.kafka.auto-offset-reset")
  val ConsumerTimoutMs            = config.getString("onyx.catalogue.kafka.consumer-timeout-ms")
  val ActorSystem                 = config.getString("onyx.catalogue.actorSystem")
  val IndexSchedulingBackoffLimit = config.getInt("onyx.catalogue.indexer.backoff-limit")
  val IndexingBatchSize           = config.getInt("onyx.catalogue.indexer.batch-size")
  val IndexDirectory              = config.getString("onyx.catalogue.indexer.index-directory")
  val IndexerCount                = config.getInt("onyx.catalogue.indexer.num-of-indexers")
  val MaxBufferedDocs             = config.getInt("onyx.catalogue.indexer.max-buffered-docs")
  val MaxRAMBufferSize            = config.getDouble("onyx.catalogue.indexer.max-ram-buffer-size")
  val IndexingTopic               = config.getString("onyx.catalogue.indexer.indexing-topic")

}

object OnyxSettings extends ExtensionId[OnyxSettings] with ExtensionIdProvider {
  override def lookup = OnyxSettings

  override def createExtension(system: ExtendedActorSystem) =
    new OnyxSettings(system.settings.config)

  override def get(system: ActorSystem): OnyxSettings = super.get(system)
}