package org.reactive.shop.products.persistence.events

import org.reactive.shop.products.model.Product

case class InsertProductEvent(product: Product) extends ProductsEvent