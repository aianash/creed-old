package creed
package core
package search
package exception

object SearchError extends Enumeration {
  type SearchError = Value
  val TooMuchLoad = Value
}