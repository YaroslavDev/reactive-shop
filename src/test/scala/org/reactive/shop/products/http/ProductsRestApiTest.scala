package org.reactive.shop.products.http

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import org.reactive.shop.products.model.{ProductListJsonSupport, ProductList, Product}
import org.reactive.shop.products.persistence.ProductsCommandActor.{UpdateProductResponse, UpdateProductCommand, InsertProductResponse, InsertProductCommand}
import org.reactive.shop.products.persistence.ProductsQueryActor.ProductsQuery
import org.scalatest.Matchers
import org.scalatest.WordSpec

import org.reactive.shop.products._

class ProductsRestApiTest extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with ProductListJsonSupport {

  val productsCommandStub = TestActorRef(new Actor {
    override def receive: Receive = {
      case InsertProductCommand(p: Product) => sender() ! InsertProductResponse(p)
      case UpdateProductCommand(p: Product) => sender() ! UpdateProductResponse(p)
    }
  })
  val productsQueryStub = TestActorRef(new Actor {
    override def receive: Receive = {
      case ProductsQuery => sender() ! List(expectedProduct)
    }
  })

  "ProductsRestApi" should {
    val productsRestApi = new ProductsRestApi(productsCommandStub, productsQueryStub)

    "return 200 OK and list of available products" in {
      Get("/api/products") ~> productsRestApi.route ~> check {
        responseAs[ProductList] shouldEqual ProductList(List(expectedProduct))
        status shouldEqual StatusCodes.OK
      }
    }

    "return 202 Accepted on product insertion" in {
      Post("/api/products", expectedProduct) ~> productsRestApi.route ~> check {
        status shouldEqual StatusCodes.Accepted
      }
    }

    "return 200 OK on product update" in {
      Put("/api/products", expectedProduct) ~> productsRestApi.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
