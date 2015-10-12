package creed
package core
package search

import commons.catalogue.attributes._

case class Query(queryStr: String, filters: Map[ClothingStyle, QueryFilters])
case class QueryRecommendations(styles: Set[ClothingStyle], filters: Map[ClothingStyle, QueryFilters])