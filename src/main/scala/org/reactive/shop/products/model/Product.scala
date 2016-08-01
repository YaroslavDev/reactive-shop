package org.reactive.shop.products.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Product(id: String, name: String, description: String, price: Float)

trait ProductJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(Product)
}

