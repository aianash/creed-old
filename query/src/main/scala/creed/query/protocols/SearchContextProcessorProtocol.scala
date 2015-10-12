package creed
package query
package protocols

import commons.catalogue.attributes._
import core.search._

sealed trait SearchContextProcessorMessages
case class ProcessForSearchContext(searchId: SearchId, query: Query, styles: Set[ClothingStyle])