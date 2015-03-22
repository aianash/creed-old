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

  val GroupId = config.getString("onyx.catalogue.kafka.group-id")
  val ZookeeperConnect = config.getString("onyx.catalogue.kafka.zookeeper-connect")
  val AutoOffsetReset = config.getString("onyx.catalogue.kafka.auto-offset-reset")
  val ConsumerTimoutMs = config.getString("onyx.catalogue.kafka.consumer-timeout-ms")
  val ActorSystem = config.getString("onyx.catalogue.actorSystem")

}

object OnyxSettings extends ExtensionId[OnyxSettings] with ExtensionIdProvider {
  override def lookup = OnyxSettings

  override def createExtension(system: ExtendedActorSystem) =
    new OnyxSettings(system.settings.config)

  override def get(system: ActorSystem): OnyxSettings = super.get(system)
}