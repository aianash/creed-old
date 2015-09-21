package creed
package core
package search

// TODO use from commons
case class UserId(uuid: Long)
case class SearchId(userId: UserId, sruid: Long)