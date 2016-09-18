package org.reactive.shop

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.reactive.shop.products.http.ProductsRestApi
import org.reactive.shop.products.persistence.{ProductsQueryActor, ProductsCommandActor}

import scala.io.StdIn

object ReactiveShop extends App {
  val port = 8080
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val PERSISTENCE_ID = "products-persistent-actor"
  val productsCommandActor = system.actorOf(Props(classOf[ProductsCommandActor], PERSISTENCE_ID))
  val productsQueryActor = system.actorOf(Props(classOf[ProductsQueryActor], PERSISTENCE_ID, materializer))
  val bindingFuture = Http().bindAndHandle(new ProductsRestApi(productsCommandActor, productsQueryActor).route, "localhost", port)
  println(s"Reactive app started. Listening on $port")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
