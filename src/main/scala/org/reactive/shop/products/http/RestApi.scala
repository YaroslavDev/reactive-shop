package org.reactive.shop.products.http

import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import org.reactive.shop.products.model.{Product, ProductList, ProductListJsonSupport}
import org.reactive.shop.products.persistence.ProductsCommandActor.{InsertProductCommand, UpdateProductCommand}
import org.reactive.shop.products.persistence.ProductsQueryActor.ProductsQuery

import scala.concurrent.Future

class RestApi(productsCommandActor: ActorRef, productsQueryActor: ActorRef) extends ProductListJsonSupport {

  val route =
    pathPrefix("api") {
      path("products") {
        get {
          onSuccess(fetchProducts) { products =>
            complete(ProductList(products))
          }
        } ~
        post {
          entity(as[Product]) { product =>
            insertProduct(product)
            complete(StatusCodes.Accepted)
          }
        } ~
        put {
          entity(as[Product]) { product =>
            updateProduct(product)
            complete(StatusCodes.OK)
          }
        }
      }
    }

  def fetchProducts: Future[List[Product]] = {
    implicit val timeout = Timeout(5, SECONDS)
    (productsQueryActor ? ProductsQuery).mapTo[List[Product]]
  }

  def insertProduct(product: Product): Unit = productsCommandActor ! InsertProductCommand(product)

  def updateProduct(product: Product): Unit = productsCommandActor ! UpdateProductCommand(product)
}
