package de.richargh.sandbox.akkajson

import akka.actor.ActorRef
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.server.HttpApp
import akka.http.javadsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.PatternsCS
import java.util.concurrent.TimeUnit
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import akka.http.javadsl.server.PathMatchers.segment
import akka.http.javadsl.server.PathMatchers.longSegment
import java.util.concurrent.CompletionStage

class UserServer(val userActor: ActorRef): HttpApp() {

    private var timeout = Timeout(FiniteDuration(5, TimeUnit.SECONDS))

    override fun routes(): Route = path(segment("users").slash(longSegment()), this::getUser)

    private fun getUser(id: Long): Route {
        return get {
            val maybeUser: CompletionStage<User> = PatternsCS.ask(userActor, GetUserMessage(id), timeout)
                    .thenApply { obj -> obj as? User }

            onSuccess({ maybeUser }, { user: User? ->
                if (user != null)
                    complete(StatusCodes.OK(), user, Jackson.marshaller())
                else
                    complete(StatusCodes.NotFound())
            })
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("userServer")
    val userActor = system.actorOf(UserActor.props(), "userActor")
    val server = UserServer(userActor)
    server.startServer("localhost", 8080, system)
}