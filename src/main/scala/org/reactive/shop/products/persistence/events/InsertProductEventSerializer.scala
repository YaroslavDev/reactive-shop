package org.reactive.shop.products.persistence.events

import java.nio.charset.Charset
import akka.serialization.SerializerWithStringManifest
import org.reactive.shop.products.model.Product

class InsertProductEventSerializer extends SerializerWithStringManifest {

  val Utf8 = Charset.forName("UTF-8")

  val INSERT_PRODUCT_EVENT_V1 = "InsertProductEventV1"

  override def identifier: Int = 42

  override def manifest(o: AnyRef): String = INSERT_PRODUCT_EVENT_V1 // current version of InsertProductEvent

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    val insertProductEventString = new String(bytes, Utf8)
    manifest match {
      case INSERT_PRODUCT_EVENT_V1 =>
        val Array(id: String, name: String, description: String, priceString: String) = insertProductEventString.split("[|]")
        InsertProductEvent(Product(id, name, description, priceString.toFloat))
      case _ => throw new IllegalArgumentException("Unsupported manifest")
    }
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case e: InsertProductEvent =>
      s"${e.product.id}|${e.product.name}|${e.product.description}|${e.product.price}".getBytes(Utf8)
    case _ => throw new IllegalArgumentException("InsertProductEventSerializer supports only InsertProductEvent's for serialization")
  }
}
