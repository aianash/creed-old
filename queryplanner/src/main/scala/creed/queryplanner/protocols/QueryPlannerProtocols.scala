// package creed.queryplanner.protocols

// import org.apache.lucene.search.BooleanQuery

// import com.goshoplane.creed.search._

// import goshoplane.commons.core.protocols._

// sealed trait QueryPlannerMessages extends Serializable

// case class BuildQuery(request: CatalogueSearchRequest) extends QueryPlannerMessages with Replyable[BooleanQuery]