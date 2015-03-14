package creed.service

import akka.actor.{ActorSystem, Extension, ExtensionId, ExtensionIdProvider, ExtendedActorSystem}

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Creed specific settings
 */
class CreedSettings(cfg: Config) extends Extension {

  // validate creed config
  final val config: Config = {
    val config = cfg.withFallback(ConfigFactory.defaultReference)
    config.checkValid(ConfigFactory.defaultReference, "creed")
    config
  }

  val ActorSystem = config.getString("creed.actorSystem")
  val CreedEndpoint = config.getString("creed.host") + ":" + config.getInt("creed.port")

}

object CreedSettings extends ExtensionId[CreedSettings] with ExtensionIdProvider {
  override def lookup = CreedSettings

  override def createExtension(system: ExtendedActorSystem) =
    new CreedSettings(system.settings.config)

  override def get(system: ActorSystem): CreedSettings = super.get(system)
}