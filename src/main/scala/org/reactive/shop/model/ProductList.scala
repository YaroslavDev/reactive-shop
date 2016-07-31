package org.reactive.shop.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

case class ProductList(products: List[Product])

trait ProductListJsonSupport extends SprayJsonSupport with ProductJsonSupport {
  implicit val productListFormat = jsonFormat1(ProductList)
}