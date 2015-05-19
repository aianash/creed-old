package creed.queryplanner.protocols

import com.goshoplane.creed.search._

sealed trait QueryPlannerMessages extends Serializable

case class BuildQuery(request: CatalogueSearchRequest) extends QueryPlannerMessages