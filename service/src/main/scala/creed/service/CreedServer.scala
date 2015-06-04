package creed.service

import com.typesafe.config.{Config, ConfigFactory}

import com.twitter.util.Await

import creed.service.injectors._
import creed.core.injectors._

object CreedServer {

  def main(args: Array[String]) {

    val config = ConfigFactory.load("creed")

    implicit val appModule = new CreedServiceModule :: new ActorSystemModule(config)

    val service = CreedService.start

  }

}