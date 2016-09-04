package org.reactive.shop.products.persistence.events

import org.reactive.shop.products.model.Product

case class UpdateProductEvent(product: Product) extends ProductsEvent

