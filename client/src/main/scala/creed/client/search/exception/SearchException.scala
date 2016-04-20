package creed
package client
package search
package exception

import SearchError._

case class SearchException(code: SearchError, debugMsg: String, friendlyMsg: String) extends Exception