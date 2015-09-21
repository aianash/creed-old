package creed
package search
package protocols

import query.SearchContext
import core.search._


sealed trait SearchSchedulerMessages
case class ScheduleSearchingFor(searchId: SearchId, context: SearchContext) extends SearchSchedulerMessages
case class ScheduleFurtherSearchingFor(searchId: SearchId, context: SearchContext) extends SearchSchedulerMessages

case class SearchResultFor(searchId: SearchId, result: SearchResult) extends SearchSchedulerMessages