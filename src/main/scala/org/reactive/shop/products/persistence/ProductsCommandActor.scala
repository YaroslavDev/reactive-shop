package org.reactive.shop.products.persistence

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}
import org.reactive.shop.products.model.Product
import org.reactive.shop.products.persistence.ProductsCommandActor._
import org.reactive.shop.products.persistence.events.{InsertProductEvent, ProductsEvent, UpdateProductEvent}

import scala.concurrent.duration._

class ProductsCommandActor(override val persistenceId: String) extends PersistentActor with ActorLogging {
  import context.dispatcher

  private var productsStore: ProductsStore = new ProductsStore

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.system.scheduler.schedule(Duration.Zero, 10 seconds, self, SnapshotProducts)
  }

  override def receiveCommand: Receive = {
    case cmd@InsertProductCommand(product: Product) =>
      log.info(s"Received InsertProductCommand: $cmd")
      persist(InsertProductEvent(product)) { insertEvent =>
        productsStore.updateState(insertEvent)
        sender() ! InsertProductResponse(product)
      }
    case cmd@UpdateProductCommand(product: Product) =>
      log.info(s"Received UpdateProductCommand: $cmd")
      persist(UpdateProductEvent(product)) { updateEvent =>
        productsStore.updateState(updateEvent)
        sender() ! UpdateProductResponse(product)
      }
    case SnapshotProducts =>
      log.info("Received SnapshotProducts command")
      saveSnapshot(productsStore)
      sender() ! SnapshotResponse
  }

  override def receiveRecover: Receive = {
    case event: ProductsEvent =>
      log.info(s"Recovering write side with event $event")
      productsStore.updateState(event)
    case SnapshotOffer(_, snapshot: ProductsStore) =>
      log.info(s"Recovering write side with snapshot $snapshot")
      productsStore = snapshot
  }
}

object ProductsCommandActor {

  trait ProductsCommand
  trait ProductsResponse

  case object SnapshotProducts extends ProductsCommand
  case object SnapshotResponse extends ProductsResponse

  case class InsertProductCommand(product: Product) extends ProductsCommand
  case class InsertProductResponse(product: Product) extends ProductsResponse

  case class UpdateProductCommand(product: Product) extends ProductsCommand
  case class UpdateProductResponse(product: Product) extends ProductsResponse
}