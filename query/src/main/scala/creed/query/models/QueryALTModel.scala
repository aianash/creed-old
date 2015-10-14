package creed
package query
package models

import core.query._, core.search._

import scalaz._, Scalaz._


class QueryALTModel {
  def alt(query: Query): Option[ALT] = ALT(Activity("party"), Look("modern"), TimeWeather("night")).some
}