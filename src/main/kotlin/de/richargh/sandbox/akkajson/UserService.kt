package de.richargh.sandbox.akkajson

class UserService {
    fun getUser(userId: Long): User? {
        println("Getting user $userId")
        return User("default")
    }

    fun createUser(user: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}