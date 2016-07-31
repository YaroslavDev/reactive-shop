package org.reactive.shop.http

import akka.http.scaladsl.server.Directives._
import org.reactive.shop.model.{ProductListJsonSupport, ProductList, Product}

object RestApi extends ProductListJsonSupport {

  val route =
    pathPrefix("api") {
      path("products") {
        get {
          complete(ProductList(List(Product("Railgun", 1000.0f))))
        }
      }
    }
}
