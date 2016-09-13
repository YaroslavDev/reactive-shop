name := """reactive-shop"""

scalaVersion := "2.11.7"

val akkaVersion = "2.4.9"
val reactiveMongoVersion = "0.11.9"
val mongoPluginVersion = "1.2.5"
val inmemoryPluginVersion = "1.3.8"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "com.github.scullxbones" %% "akka-persistence-mongo-rxmongo" % mongoPluginVersion,

  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % inmemoryPluginVersion % "test"
)

licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))

fork in run := true
