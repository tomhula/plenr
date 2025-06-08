package cz.tomashula.plenr.frontend

import cz.tomashula.plenr.feature.user.UserDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

actual fun createAuthStorage(): AuthStorage = JvmAuthStorage()

private val AUTH_FILE_PATH = Path("auth.json")

class JvmAuthStorage : AuthStorage
{
    override fun storeAuth(token: String, user: UserDto)
    {
        val auth = Auth(token, user)
        val authStr = Json.encodeToString(auth)
        AUTH_FILE_PATH.writeText(authStr)
    }

    override fun getAuthToken(): String?
    {
        if (!AUTH_FILE_PATH.exists()) 
            return null
        
        val authStr = AUTH_FILE_PATH.readText()
        return Json.decodeFromString<Auth>(authStr).token
    }

    override fun getUser(): UserDto?
    {
        if (!AUTH_FILE_PATH.exists()) 
            return null
        
        val authStr = AUTH_FILE_PATH.readText()
        return Json.decodeFromString<Auth>(authStr).user
    }

    override fun clear()
    {
        AUTH_FILE_PATH.deleteIfExists()
    }
    
    @Serializable
    data class Auth(
        val token: String,
        val user: UserDto
    )
}
