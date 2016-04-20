package creed
package service

import com.typesafe.config.{Config, ConfigFactory}

import akka.actor.ActorSystem
import akka.serialization._

import commons.microservice.Microservice

import components._

import commons.catalogue._, collection._


object CreedServer {

  def main(args: Array[String]) {
    val config = ConfigFactory.load("creed")
    val system = ActorSystem(config.getString("creed.actorSystem"), config)
    // println(SerializationExtension(system).findSerializerFor(CatalogueItems(Seq.empty[CatalogueItem])))
    Microservice(system).start(IndexedSeq(SearchComponent))
  }

}