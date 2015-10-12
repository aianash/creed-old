package creed
package core
package search

import neutrino.core.user.UserId

// case class UserId(uuid: Long)
case class SearchId(userId: UserId, sruid: Long)