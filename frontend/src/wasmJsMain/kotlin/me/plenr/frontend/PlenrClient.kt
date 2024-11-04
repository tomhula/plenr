package me.plenr.frontend

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.browser.window
import kotlinx.rpc.krpc.ktor.client.installRPC
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import me.tomasan7.plenr.feature.user.UserDto
import me.tomasan7.plenr.feature.user.UserService

class PlenrClient
{
    private val httpClient = HttpClient(Js) {
        /*defaultRequest {
            url(window.origin + "/api/")
        }*/
        /*install(ContentNegotiation) {
            json()
        }*/
        installRPC()
    }
    private lateinit var userService: UserService
    private var authToken: String? = null

    suspend fun init()
    {
        userService = httpClient.rpc {
            url {
                host = window.location.hostname
                port = getCurrentPort()
                encodedPath = "/api"
            }

            rpcConfig {
                serialization {
                    json()
                }
            }
        }.withService<UserService>()
    }

    suspend fun adminExists() = userService.adminExists()

    suspend fun createUser(user: UserDto) = userService.createUser(user, authToken!!)

    suspend fun setPassword(tokenB64: String, password: String)
    {
        val token = tokenB64.decodeBase64Bytes()
        userService.setPassword(token, password)
    }

    suspend fun login(username: String, password: String): Boolean
    {
        authToken = userService.login(username, password) ?: return false
        println("New authToken: $authToken")
        return true
    }
}