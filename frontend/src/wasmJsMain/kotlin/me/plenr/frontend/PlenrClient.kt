package me.plenr.frontend

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
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
}