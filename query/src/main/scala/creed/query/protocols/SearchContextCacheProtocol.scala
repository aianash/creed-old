package creed
package query
package protocols

import client.search.SearchId

import goshoplane.commons.core.protocols._


sealed trait SearchContextCacheMessages
case class CacheFor(searchId: SearchId, context: SearchContext) extends SearchContextCacheMessages
case class HasQueryStrChangedFor(searchId: SearchId, queryStr: String) extends SearchContextCacheMessages with Replyable[Boolean]