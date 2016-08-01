package org.reactive.shop

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.reactive.shop.products.http.RestApi
import org.reactive.shop.products.persistence.ProductsPersistentActor

import scala.io.StdIn

object ReactiveShop extends App {
  val port = 8080
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val productsActor = system.actorOf(Props[ProductsPersistentActor])
  val bindingFuture = Http().bindAndHandle(new RestApi(productsActor).route, "localhost", port)
  println(s"Reactive app started. Listening on $port")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
