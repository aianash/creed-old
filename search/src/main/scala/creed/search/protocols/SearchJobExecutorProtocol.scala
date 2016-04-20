package creed
package search
package protocols

import client.search.{SearchId, SearchResult}


sealed trait SearchJobExecutorMessages
case class SearchResultComputedFor(searchId: SearchId, result: SearchResult) extends SearchJobExecutorMessages
case class TerminateJobExecution(searchId: SearchId) extends SearchJobExecutorMessages