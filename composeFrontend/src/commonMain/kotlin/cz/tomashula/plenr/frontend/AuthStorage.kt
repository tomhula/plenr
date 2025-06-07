package cz.tomashula.plenr.frontend

import cz.tomashula.plenr.feature.user.UserDto

interface AuthStorage
{
    fun storeAuth(token: String, user: UserDto)
    fun getAuthToken(): String?
    fun getUser(): UserDto?
    fun clear()
}

expect fun createAuthStorage(): AuthStorage
