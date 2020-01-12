package de.richargh.sandbox.akkajson

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.server.HttpApp
import akka.http.javadsl.server.PathMatchers.longSegment
import akka.http.javadsl.server.PathMatchers.segment
import akka.http.javadsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.PatternsCS
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

class UserRoutes(private val userActor: ActorRef): HttpApp() {

    private var timeout = Timeout(FiniteDuration(5, TimeUnit.SECONDS))

    override fun routes(): Route = path("users", this::postUser)
            .orElse(path(segment("users").slash(longSegment())) { userId -> getUser(userId) })

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

    private fun postUser(): Route = put {
        entity(Jackson.unmarshaller(User::class.java)) { user ->
            val userCreated = PatternsCS.ask(userActor, CreateUserMessage(user), timeout)
                    .thenApply { obj -> obj as ActionPerformed }

            onSuccess({ userCreated }, { performed ->
                complete(StatusCodes.Created(), performed, Jackson.marshaller())
            })
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("userServer")
    val userActor = system.actorOf(UserActor.props(), "userActor")
    val server = UserRoutes(userActor)
    server.startServer("localhost", 8080, system)
}