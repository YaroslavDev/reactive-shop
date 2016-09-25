package org.reactive.shop.products.http

import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import org.reactive.shop.products.model.{Product, ProductList, ProductListJsonSupport}
import org.reactive.shop.products.persistence.ProductsCommandActor._
import org.reactive.shop.products.persistence.ProductsQueryActor.ProductsQuery

import scala.concurrent.Future

class ProductsRestApi(productsCommandActor: ActorRef, productsQueryActor: ActorRef) extends ProductListJsonSupport {

  val route =
    pathPrefix("api") {
      pathPrefix("products") {
        get {
          onSuccess(fetchProducts) { products =>
            complete(ProductList(products))
          }
        } ~
        post {
          entity(as[Product]) { product =>
            onSuccess(insertProduct(product)) { response =>
              complete(StatusCodes.Accepted)
            }
          }
        } ~
        put {
          entity(as[Product]) { product =>
            onSuccess(updateProduct(product)) { response =>
              complete(StatusCodes.OK)
            }
          }
        } ~
        path("snapshot") {
          post {
            entity(as[String]) { _ =>
              productsCommandActor ! SnapshotProducts
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }

  implicit val timeout = Timeout(5, SECONDS)

  def fetchProducts: Future[List[Product]] = {
    (productsQueryActor ? ProductsQuery).mapTo[List[Product]]
  }

  def insertProduct(product: Product): Future[InsertProductResponse] = {
    (productsCommandActor ? InsertProductCommand(product)).mapTo[InsertProductResponse]
  }

  def updateProduct(product: Product): Future[UpdateProductResponse] = {
    (productsCommandActor ? UpdateProductCommand(product)).mapTo[UpdateProductResponse]
  }
}
