package me.plenr.frontend

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import me.tomasan7.plenr.api.SetPasswordDto
import me.tomasan7.plenr.api.UserDto
import web.window

class PlenrClient
{
    private val httpClient = HttpClient(Js) {
        defaultRequest {
            url(window.origin + "/api/")
        }
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun adminExists(): Boolean
    {
        return httpClient.get("admin-exists").body()
    }

    suspend fun createUser(user: UserDto): Int
    {
        return httpClient.post("user") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    suspend fun setPassword(tokenB64: String, password: String)
    {
        val token = tokenB64.decodeBase64Bytes()

        httpClient.post("set-password") {
            contentType(ContentType.Application.Json)
            setBody(SetPasswordDto(token, password))
        }
    }
}