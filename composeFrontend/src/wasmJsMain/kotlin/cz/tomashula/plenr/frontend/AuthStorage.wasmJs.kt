package cz.tomashula.plenr.frontend

import cz.tomashula.plenr.feature.user.UserDto
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json

actual fun createAuthStorage(): AuthStorage = JsAuthStorage()

private const val AUTH_TOKEN_STORAGE_KEY = "authToken"
private const val USER_STORAGE_KEY = "user"

class JsAuthStorage : AuthStorage
{
    override fun storeAuth(token: String, user: UserDto)
    {
        localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token)
        localStorage.setItem(USER_STORAGE_KEY, Json.encodeToString(user))
    }

    override fun getAuthToken(): String?
    {
        return localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
    }

    override fun getUser(): UserDto?
    {
        return localStorage.getItem(USER_STORAGE_KEY)?.let { Json.decodeFromString(it) }
    }

    override fun clear()
    {
        localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
        localStorage.removeItem(USER_STORAGE_KEY)
    }
}
