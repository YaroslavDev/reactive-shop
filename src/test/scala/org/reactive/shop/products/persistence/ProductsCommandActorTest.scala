package org.reactive.shop.products.persistence

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.reactive.shop.products.expectedProduct
import org.reactive.shop.products.persistence.ProductsCommandActor._
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class ProductsCommandActorTest extends TestKit(ActorSystem("ProductsCommandActorTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with ImplicitSender {

  var persistentId = ""
  var commandsActor1: ActorRef = null

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override protected def beforeEach(): Unit = {
    persistentId = UUID.randomUUID().toString
    commandsActor1 = system.actorOf(Props(classOf[ProductsCommandActor], persistentId))
  }

  "ProductsCommandActor" should {
    val timeout = 5 seconds

    "insert new product" in {
      commandsActor1 ! InsertProductCommand(expectedProduct)
      expectMsg(timeout, InsertProductResponse(expectedProduct))

      commandsActor1 ! PoisonPill

      val commandsActor2 = system.actorOf(Props(classOf[ProductsCommandActor], persistentId))
      commandsActor2 ! GetProductsStore
      expectMsg(timeout, ProductsStore(List(expectedProduct)))
    }

    "update existing product" in {
      commandsActor1 ! InsertProductCommand(expectedProduct)
      expectMsg(timeout, InsertProductResponse(expectedProduct))
      val updatedProduct = expectedProduct.copy(price = 2000.0f)
      commandsActor1 ! UpdateProductCommand(updatedProduct)
      expectMsg(timeout, UpdateProductResponse(updatedProduct))

      commandsActor1 ! PoisonPill

      val commandsActor2 = system.actorOf(Props(classOf[ProductsCommandActor], persistentId))
      commandsActor2 ! GetProductsStore
      expectMsg(timeout, ProductsStore(List(updatedProduct)))
    }
  }

}