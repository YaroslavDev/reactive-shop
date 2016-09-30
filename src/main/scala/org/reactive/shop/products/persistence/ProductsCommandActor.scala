package org.reactive.shop.products.persistence

import akka.actor.ActorLogging
import akka.persistence._
import org.reactive.shop.products.model.Product
import org.reactive.shop.products.persistence.ProductsCommandActor._
import org.reactive.shop.products.persistence.events.{InsertProductEvent, ProductsEvent, UpdateProductEvent}

class ProductsCommandActor(override val persistenceId: String) extends PersistentActor with ActorLogging {

  private var productsStore: ProductsStore = new ProductsStore

  override def receiveCommand: Receive = {
    case cmd@InsertProductCommand(product: Product) =>
      log.info(s"Inserting new product $product")
      persist(InsertProductEvent(product)) { insertEvent =>
        productsStore.updateState(insertEvent)
        sender() ! InsertProductResponse(product)
      }
    case cmd@UpdateProductCommand(product: Product) =>
      log.info(s"Updating existing product $product")
      persist(UpdateProductEvent(product)) { updateEvent =>
        productsStore.updateState(updateEvent)
        sender() ! UpdateProductResponse(product)
      }
    case SnapshotProducts =>
      log.info("Saving snapshot of current productsStore")
      saveSnapshot(productsStore)
    case GetProductsStore =>
      sender() ! productsStore
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

  case class InsertProductCommand(product: Product) extends ProductsCommand
  case class InsertProductResponse(product: Product) extends ProductsResponse

  case class UpdateProductCommand(product: Product) extends ProductsCommand
  case class UpdateProductResponse(product: Product) extends ProductsResponse

  case object GetProductsStore extends ProductsCommand
}