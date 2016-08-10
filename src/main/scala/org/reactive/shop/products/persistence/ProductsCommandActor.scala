package org.reactive.shop.products.persistence

import akka.actor.ActorLogging
import akka.persistence.{SnapshotOffer, PersistentActor}
import org.reactive.shop.products.model.Product
import org.reactive.shop.products.persistence.ProductsCommandActor._
import scala.concurrent.duration._

class ProductsCommandActor extends PersistentActor with ActorLogging {
  import context.dispatcher

  override def persistenceId: String = "products-persistent-actor"

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
      }
    case cmd@UpdateProductCommand(product: Product) =>
      log.info(s"Received UpdateProductCommand: $cmd")
      persist(UpdateProductEvent(product)) { updateEvent =>
        productsStore.updateState(updateEvent)
      }
    case SnapshotProducts =>
      log.info("Received SnapshotProducts command")
      saveSnapshot(productsStore)
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
  val PERSISTENCE_ID = "products-persistent-actor"

  trait ProductsCommand
  case object SnapshotProducts extends ProductsCommand
  case class InsertProductCommand(product: Product) extends ProductsCommand
  case class UpdateProductCommand(product: Product) extends ProductsCommand

  trait ProductsEvent
  case class InsertProductEvent(product: Product) extends ProductsEvent
  case class UpdateProductEvent(product: Product) extends ProductsEvent
}