package creed
package query

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Props, ActorSelection}

import commons.catalogue._, attributes._

import core.query._, core.search._, core.search.protocols._
import client.search._
import models._

class QueryRecommender(backchannel: ActorSelection) extends Actor {

  import QueryRecommender._
  import protocols._

  val searchContext = context.actorSelection("../searchContext")
  val model = new StyleRecommendationModel
  val stylesCache = new ConcurrentHashMap[SearchId, Set[ClothingStyle]]

  def receive = {
    case RecommendFor(searchId, query, alt) =>
      println("recommender")
      model.styles(alt)
        .foreach { styles =>
          if(isNewFor(searchId, styles)) {
            backchannel ! SendThruBackchannelFor(searchId, QueryRecommendationsFor(searchId, QueryRecommendations(styles, Filters(styles))))
            searchContext ! ProcessForSearchContext(searchId, query, styles)
            println("send to process for search context")
          }
        }
  }

  private def isNewFor(searchId: SearchId, styles: Set[ClothingStyle]) =
    stylesCache.get(searchId) match {
      case null => stylesCache.put(searchId, styles); true
      case oldStyles if(oldStyles.size == styles.size) =>
        val union =  styles union oldStyles
        if(union.size == styles.size && union.size == oldStyles.size) false
        else {
          stylesCache.put(searchId, styles)
          true
        }
      case _ => stylesCache.put(searchId, styles); true
    }

}

object QueryRecommender {
  def props(backchannel: ActorSelection) = Props(classOf[QueryRecommender], backchannel)

  object Filters {

    import ClothingSize._
    import ItemTypeGroup._

    private val filterMap = Map(
      WomensTops -> QueryFilters(
        ColorFilter("red", "blue", "green"),
        SizesFilter(S, M, L, XL, XXL)
      )
    )

    def apply(styles: Set[ClothingStyle]) =
      styles.foldLeft(Map.empty[ClothingStyle, QueryFilters]) { (filters, style) =>
        filters + (style -> filterMap(ClothingStyle.group(style)))
      }
  }

}