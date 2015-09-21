package creed
package core
package search

case class Query(queryStr: String, filter: QueryFilter)
case class QueryFilter()