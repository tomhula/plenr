package cz.tomashula.plenr.frontend

import cz.tomashula.plenr.feature.user.UserDto

actual fun createAuthStorage(): AuthStorage = JvmAuthStorage()

class JvmAuthStorage : AuthStorage
{
    override fun storeAuth(token: String, user: UserDto)
    {
        println("Storing auth token: $token")
        println("Storing user: $user")
    }

    override fun getAuthToken(): String?
    {
        println("Retrieving auth token")
        return null
    }

    override fun getUser(): UserDto?
    {
        println("Retrieving user")
        return null
    }

    override fun clear()
    {
        println("Clearing auth storage")
    }
}
