package creed.service

import akka.actor.{Actor, Props}

import java.io.File

import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.DirectoryReader

import creed.queryplanner._

/**
 * This class acts as searching supervisor.
 * Currently it forwards the request to catalogue searcher
 */
class SearchingSupervisor extends Actor {

  import creed.service.protocols._
  import context._

  val settings          = CreedSettings(context.system)
  var searchDir         = FSDirectory.open(new File(settings.SearchDirectory), null)
  val reader            = DirectoryReader.open(searchDir)
  val searcher          = new IndexSearcher(reader)
  val queryPlanner      = context.actorOf(QueryPlanner.props, "queryPlanner")
  val catalogueSearcher = context.actorOf(CatalogueSearcher.props(searcher, queryPlanner), "catalogueSearcher")

  def receive = {
    case msg: SearchCatalogue =>
      catalogueSearcher forward msg
  }

}

/**
 * Companion object
 */
object SearchingSupervisor {

  def props = Props(classOf[SearchingSupervisor])

}