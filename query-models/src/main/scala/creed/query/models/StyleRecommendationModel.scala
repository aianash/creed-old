package creed
package query
package models

import commons.catalogue.attributes._

import scalaz._, Scalaz._

class StyleRecommendationModel {
  def styles(alt: ALT): Option[Set[ClothingStyle]] = Set(ClothingStyle.TeesTop).some
}
