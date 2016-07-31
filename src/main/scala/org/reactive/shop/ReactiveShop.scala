package org.reactive.shop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.reactive.shop.http.RestApi

import scala.io.StdIn

object ReactiveShop extends App {
  val port = 8080
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(RestApi.route, "localhost", port)
  println(s"Reactive app started. Listening on $port")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
