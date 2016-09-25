package org.reactive.shop.products.persistence

import akka.NotUsed
import akka.actor.{Actor, ActorLogging}
import akka.contrib.persistence.mongodb.{MongoReadJournal, ScalaDslMongoReadJournal}
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.reactive.shop.products.persistence.ProductsQueryActor.ProductsQuery
import org.reactive.shop.products.persistence.events.ProductsEvent

class ProductsQueryActor(val persistenceId: String, implicit val materializer: ActorMaterializer) extends Actor with ActorLogging {

  private val productsStore: ProductsStore = new ProductsStore

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    val eventEnvelopes = eventSource
    val events = eventEnvelopes.map(_.event)
    events.runForeach {
      case productsEvent: ProductsEvent =>
        log.info(s"Updating read side with event $productsEvent")
        productsStore.updateState(productsEvent)
      case any =>
        log.info(s"Read side received something else $any")
    }
  }

  override def receive: Actor.Receive = {
    case ProductsQuery =>
      sender() ! productsStore.getProducts
  }

  /**
    * MongoDB event source
    */
  protected def eventSource: Source[EventEnvelope, NotUsed] = {
    val journal = PersistenceQuery(context.system).readJournalFor[ScalaDslMongoReadJournal](MongoReadJournal.Identifier)
    journal.eventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
  }
}

object ProductsQueryActor {
  case object ProductsQuery
}