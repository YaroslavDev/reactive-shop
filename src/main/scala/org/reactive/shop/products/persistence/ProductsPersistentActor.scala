package org.reactive.shop.products.persistence

import akka.actor.ActorLogging
import akka.persistence.{SnapshotOffer, PersistentActor}
import org.reactive.shop.products.model.Product
import org.reactive.shop.products.persistence.ProductsPersistentActor._

class ProductsPersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String = "products-persistent-actor"

  private var productsStore: ProductsStore = new ProductsStore

  override def receiveCommand: Receive = {
    case ProductsQuery =>
      sender() ! productsStore.getProducts
    case InsertProductCommand(product: Product) =>
      log.info("Received InsertProductCommand")
      persist(InsertProductEvent(product)) { insertEvent =>
        productsStore.updateState(insertEvent)
      }
    case UpdateProductCommand(product: Product) =>
      log.info("Received UpdateProductCommand")
      persist(UpdateProductEvent(product)) { updateEvent =>
        productsStore.updateState(updateEvent)
      }
    case SnapshotProducts => saveSnapshot(productsStore)
  }

  override def receiveRecover: Receive = {
    case event: ProductsEvent => productsStore.updateState(event)
    case SnapshotOffer(_, snapshot: ProductsStore) => productsStore = snapshot
  }

}

object ProductsPersistentActor {

  case object ProductsQuery

  trait ProductsCommand
  case object SnapshotProducts extends ProductsCommand
  case class InsertProductCommand(product: Product) extends ProductsCommand
  case class UpdateProductCommand(product: Product) extends ProductsCommand

  trait ProductsEvent
  case class InsertProductEvent(product: Product) extends ProductsEvent
  case class UpdateProductEvent(product: Product) extends ProductsEvent
}