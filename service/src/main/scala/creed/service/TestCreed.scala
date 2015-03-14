package creed.service

import com.twitter.finagle.Thrift
import com.twitter.util.{Future => TwitterFuture, Await}

import com.goshoplane.common._
import com.goshoplane.creed.service._
import com.goshoplane.creed.search._

object TestCreed {

  def main(args: Array[String]) {

    val client = Thrift.newIface[Creed.FutureIface]("127.0.0.1:1601")
    val userId = UserId(1L)
    val query = CatalogueSearchQuery(queryText = "search")
    val f = client.searchCatalogue(CatalogueSearchRequest(userId, query))

    Await.ready(f)

    f onSuccess { response =>
      println(response)
    }

    f onFailure {
      case ex: Exception => ex.printStackTrace
    }

  }
}