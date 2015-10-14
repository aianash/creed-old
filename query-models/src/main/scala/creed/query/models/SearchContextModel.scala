package creed
package query
package models

import client.search._

import commons.catalogue.attributes._


class SearchContextModel {
  def searchContext(searchId: SearchId, query: Query, styles: Set[ClothingStyle]) = SearchContext(searchId)
}