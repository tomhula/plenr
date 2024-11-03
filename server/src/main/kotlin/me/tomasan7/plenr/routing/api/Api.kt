package me.tomasan7.plenr.routing.api

import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import me.tomasan7.plenr.Plenr
import me.tomasan7.plenr.feature.user.DatabaseUserService
import me.tomasan7.plenr.feature.user.UserService

fun Routing.apiRoute(
    plenr: Plenr
)
{
    rpc("/api")
    {
        rpcConfig {
            serialization {
                json()
            }
        }

        val serverUrl = runBlocking { plenr.getConfig().server.url }

        registerService<UserService> { ctx ->
            DatabaseUserService(
                ctx,
                serverUrl,
                plenr.database,
                plenr.passwordValidator,
                plenr.passwordHasher,
                plenr.tokenGenerator,
                plenr.mailService
            ).also { runBlocking { it.createIfNotExists() } }
        }
    }
}