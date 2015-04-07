package creed.service

import akka.actor.Actor

import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher

/**
 * This class acts as searching supervisor.
 * Currently it forwards the request to catalogue searcher
 */
class SearchingSupervisor(reader: IndexReader) extends Actor {

  import creed.service.protocols._
  import context._

  val searcher = new IndexSearcher(reader)
  val catalogueSearcher = context.actorOf(CatalogueSearcher.props(searcher), "catalogueSearcher")

  def receive = {
    case msg: SearchCatalogue => catalogueSearcher forward msg
  }

}