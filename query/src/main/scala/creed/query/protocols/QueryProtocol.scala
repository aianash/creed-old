package creed
package query
package protocols

import core.search.{SearchId, Query}

import goshoplane.commons.core.protocols._

sealed trait QueryMessages
case class GetSearchContextFor(searchId: SearchId) extends QueryMessages with Replyable[SearchContext]
case class ProcessQueryFor(searchId: SearchId, query: Query) extends QueryMessages
case class QueryIsServedFor(searchId: SearchId) extends QueryMessages
