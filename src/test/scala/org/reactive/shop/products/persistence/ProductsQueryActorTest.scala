package org.reactive.shop.products.persistence

import java.util.UUID

import akka.NotUsed
import akka.actor.{Props, ActorSystem}
import akka.persistence.inmemory.query.scaladsl.InMemoryReadJournal
import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.reactive.shop.products.persistence.ProductsCommandActor.{InsertProductResponse, InsertProductCommand}
import org.reactive.shop.products.persistence.ProductsQueryActor.ProductsQuery
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.reactive.shop.products.expectedProduct
import akka.pattern.ask

import scala.concurrent.duration._

class ProductsQueryActorTest extends TestKit(ActorSystem("ProductsQueryActorTest"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with ImplicitSender
  with ScalaFutures {

  var persistentId = ""
  val timeout = 5 seconds
  implicit val futureTimeout = Timeout(timeout)

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override protected def beforeEach(): Unit = {
    persistentId = UUID.randomUUID().toString
    val commandsActor = system.actorOf(Props(classOf[ProductsCommandActor], persistentId))
    commandsActor ! InsertProductCommand(expectedProduct)
    expectMsg(timeout, InsertProductResponse(expectedProduct))
  }

  "ProductsQueryActor" should {
    "return all products" in {
      val actorMaterializer = ActorMaterializer()
      val queryActor = system.actorOf(Props(classOf[TestableQueryActor], persistentId, actorMaterializer))

      awaitCond({
        val actualProducts = (queryActor ? ProductsQuery).mapTo[List[Product]].futureValue
        actualProducts.size == 1 && actualProducts.head == expectedProduct
      }, timeout, 1 second, s"Query actor should contain list of products that contains single $expectedProduct")
    }
  }
}

class TestableQueryActor(override val persistenceId: String,
                         override implicit val materializer: ActorMaterializer) extends ProductsQueryActor(persistenceId, materializer) {
  /**
    * InMemory event source
    */
  override protected def eventSource: Source[EventEnvelope, NotUsed] = {
    val journal = PersistenceQuery(context.system).readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)
    journal.eventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
  }
}