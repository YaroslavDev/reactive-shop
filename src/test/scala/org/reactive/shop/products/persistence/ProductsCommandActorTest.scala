package org.reactive.shop.products.persistence

import java.util.UUID
import java.util.concurrent.TimeUnit._

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.reactive.shop.products.expectedProduct
import org.reactive.shop.products.persistence.ProductsCommandActor.{InsertProductCommand, InsertProductResponse}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ProductsCommandActorTest extends TestKit(ActorSystem("ProductsCommandActorTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ImplicitSender {

  implicit val timeout = Timeout(5, SECONDS)
  //implicit val patience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ProductsCommandActor" should {
    val persistentId = UUID.randomUUID().toString
    val commandsActor = system.actorOf(Props(classOf[ProductsCommandActor], persistentId))

    "insert new product" in {
      commandsActor ! InsertProductCommand(expectedProduct)
      expectMsg(InsertProductResponse(expectedProduct))
    }
  }

}