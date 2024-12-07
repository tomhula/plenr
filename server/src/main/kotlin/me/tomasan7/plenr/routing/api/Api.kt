package me.tomasan7.plenr.routing.api

import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import me.tomasan7.plenr.Plenr
import me.tomasan7.plenr.feature.training.DatabaseTrainingService
import me.tomasan7.plenr.feature.training.TrainingService
import me.tomasan7.plenr.feature.user.DatabaseTempBusyTimesService
import me.tomasan7.plenr.feature.user.DatabaseUserPreferencesService
import me.tomasan7.plenr.feature.user.DatabaseUserService
import me.tomasan7.plenr.feature.user.UserService
import me.tomasan7.plenr.feature.user.preferences.UserPreferencesService
import me.tomasan7.plenr.feature.user.tempbusytimes.TempBusyTimesService

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
                plenr.mailService,
                plenr.authService
            ).also { runBlocking { it.createIfNotExists() } }
        }

        registerService<TrainingService> { ctx ->
            DatabaseTrainingService(
                ctx,
                plenr.database,
                serverUrl,
                plenr.authService,
                plenr.mailService
            ).also { runBlocking { it.createIfNotExists() } }
        }

        registerService<UserPreferencesService> { ctx ->
            DatabaseUserPreferencesService(
                ctx,
                plenr.database,
                plenr.authService
            ).also { runBlocking { it.createIfNotExists() } }
        }

        registerService<TempBusyTimesService> { ctx ->
            DatabaseTempBusyTimesService(
                plenr.database,
                ctx,
                plenr.authService
            ).also { runBlocking { it.createIfNotExists() } }
        }
    }
}