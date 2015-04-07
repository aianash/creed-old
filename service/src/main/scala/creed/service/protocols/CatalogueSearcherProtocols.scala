package creed.service.protocols

import com.goshoplane.creed.search._

sealed trait CatalogueSearcherProtocols extends Serializable

case class SearchCatalogue(request: CatalogueSearchRequest) extends CatalogueSearcherProtocols