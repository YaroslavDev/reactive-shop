# Reactive Shop

This is a application containing simple REST API for adding, updating and fetching products from imaginary shop.

## REST API

REST API part was implemented using Akka HTTP module. See [ProductsRestApi](src/main/scala/org/reactive/shop/products/http/ProductsRestApi.scala)

* POST /api/products - *Creates and persists product*

```json
{
	"id": "123",
	"name": "Nuclear engine",
	"description": "Used for building spaceships",
	"price": 1000.0
}
```

* PUT /api/products - *Updates existing product by existing*

```json
{
	"id": "123",
	"name": "Nuclear engine",
	"description": "Used for building spaceships",
	"price": 2000.0
}
```

* GET /api/products - *Get list of available products*

* POST /api/products/snapshot - *Create a snapshot of events received up to moment of calling*

## Data model

Entity like Product is accessed using ES/CQRS style, not classic CRUD. 
See [ProductsCommandActor](src/main/scala/org/reactive/shop/products/persistence/ProductsCommandActor.scala).
Product commands:

* InsertProductCommand - *sent to ProductsCommandActor when new product needs to be inserted.* 
This command causes [InsertProductEvent](src/main/scala/org/reactive/shop/products/persistence/events/InsertProductEvent.scala) to be persisted.
* UpdateProductCommand - *sent to ProductsCommandActor when existing product needs to be updated.* 
This command causes [UpdateProductCommand](src/main/scala/org/reactive/shop/products/persistence/events/UpdateProductEvent.scala) to be persisted.

See [ProductsQueryActor](src/main/scala/org/reactive/shop/products/persistence/ProductsQueryActor.scala).
Product queries:

* ProductsQuery - *sent to ProductsQueryActor when list of stored products needs to be fetched.*
This query just fetched list of products from in-memory data structure inside ProductsQuery.

`ProductsQueryActor` on startup creates a Akka stream of events using Akka Persistence Query module and creates a in-memory representation of actual
products list.

## Installation

**Note:** You can use 3-rd parties with lower versions, but that wasn't proved to work.

You need: 

1. Scala >= 2.11.7
2. Sbt >= 0.13.8
3. MongoDB >= 3.0.2

## Running

Start MongoDB with simple `mongod` command and run application with `sbt run`. That should create DB in Mongo and setup all collections. 
Try sending REST requests (for example from Postman) and you should see some events populated in Mongo collections.

## Testing

* [ProductsCommandActorTest](src/test/scala/org/reactive/shop/products/persistence/ProductsCommandActorTest.scala): Integration tests for [ProductsCommandActor](src/main/scala/org/reactive/shop/products/persistence/ProductsCommandActor.scala) were implemented as described in http://tudorzgureanu.com/akka-persistence-testing-persistent-actors/.
* [ProductsQueryActorTest](src/test/scala/org/reactive/shop/products/persistence/ProductsQueryActorTest.scala): Integration tests for
[ProductsQueryActor](src/main/scala/org/reactive/shop/products/persistence/ProductsQueryActor.scala).
* [ProductsRestApiTest](src/test/scala/org/reactive/shop/products/http/ProductsRestApiTest.scala): Integration tests for [ProductsRestApi](src/main/scala/org/reactive/shop/products/http/ProductsRestApi.scala) are implemented using `akka-http-testkit`.
