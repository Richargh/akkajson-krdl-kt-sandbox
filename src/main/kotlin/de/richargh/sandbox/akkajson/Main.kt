package de.richargh.sandbox.akkajson

import akka.Done
import akka.actor.ActorSystem
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.longSegment
import akka.http.javadsl.server.Route
import akka.stream.ActorMaterializer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class HttpServerMinimalExampleTest: AllDirectives() {
    // (fake) async database query api
    private fun fetchItem(itemId: Long): CompletionStage<Optional<Item>> {
        return CompletableFuture.completedFuture(Optional.of(Item("foo", itemId)))
    }

    // (fake) async database query api
    private fun saveOrder(order: Order): CompletionStage<Done> {
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    fun createRoute(): Route = concat(
            get {
                pathPrefix("item") {
                    path(longSegment()) { id ->
                        val futureMaybeItem = fetchItem(id)
                        onSuccess(futureMaybeItem) { maybeItem ->
                            maybeItem.map { item ->
                                completeOK(item, Jackson.marshaller())
                            }.orElseGet {
                                complete(StatusCodes.NOT_FOUND, "Not Found")
                            }
                        }
                    }
                }
            })
}

fun main(args: Array<String>) {
    // boot up server using the route as defined below
    val system = ActorSystem.create("routes")

    val http = Http.get(system)
    val materializer = ActorMaterializer.create(system)

    // In order to access all directives we need an instance where the routes are defined
    val app = HttpServerMinimalExampleTest()

    val routeFlow = app.createRoute().flow(system, materializer)
    val binding = http.bindAndHandle(routeFlow,
                                     ConnectHttp.toHost("localhost", 8080), materializer)

    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    System.`in`.read() // let it run until user presses return

    binding
            .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
            .thenAccept { system.terminate() } // and shutdown when done
}