package creed
package search
package protocols

import client.search.SearchId
import query.SearchContext

sealed trait SearcherProtocol
case class ExecuteSearchFor(searchId: SearchId, searchContext: SearchContext) extends SearcherProtocol
case class StopSearchingFor(searchId: SearchId) extends SearcherProtocol