package creed
package query
package protocols

import client.search.SearchId


sealed trait SearchContextCacheMessages
case class CacheFor(searchId: SearchId, context: SearchContext) extends SearchContextCacheMessages