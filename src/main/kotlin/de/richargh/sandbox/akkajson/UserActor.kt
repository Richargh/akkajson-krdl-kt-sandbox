package de.richargh.sandbox.akkajson

import akka.actor.AbstractActor
import akka.actor.Props
import akka.japi.pf.FI

internal class UserActor: AbstractActor() {

    private val userService = UserService()

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(GetUserMessage::class.java, handleGetUser())
                .match(CreateUserMessage::class.java, handleCreateUser())
                .build()
    }

    private fun handleGetUser() = FI.UnitApply<GetUserMessage> { getUserMessage ->
        sender().tell(userService.getUser(getUserMessage.userId), self)
    }

    private fun handleCreateUser(): FI.UnitApply<CreateUserMessage> {
        return FI.UnitApply { createUserMessage ->
            userService.createUser(createUserMessage.user)

            sender().tell(ActionPerformed(
                            String.format("User %s created.", createUserMessage.user.name)), self)
        }
    }

    companion object {
        fun props(): Props {
            return Props.create(UserActor::class.java) { UserActor() }
        }
    }
}

class ActionPerformed(val action: String)

class GetUserMessage(val userId: Long)

class CreateUserMessage(val user: User)

class User(val name: String)