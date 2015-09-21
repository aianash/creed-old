// package creed.core

// import akka.actor.ActorSystem

// import scaldi.Module
// import scaldi.Injectable._

// import com.typesafe.config.{Config, ConfigFactory}

// package object injectors {
//   class ActorSystemModule(cfg: Config) extends Module {
//     bind [ActorSystem] to ActorSystem(cfg.getString("creed.actorSystem"), cfg)
//   }
// }