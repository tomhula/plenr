package cz.tomashula.plenr.routing.api

import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import cz.tomashula.plenr.Plenr
import cz.tomashula.plenr.feature.training.DatabaseTrainingService
import cz.tomashula.plenr.feature.training.TrainingService
import cz.tomashula.plenr.feature.user.DatabaseTempBusyTimesService
import cz.tomashula.plenr.feature.user.DatabaseUserPreferencesService
import cz.tomashula.plenr.feature.user.DatabaseUserService
import cz.tomashula.plenr.feature.user.UserService
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesService
import cz.tomashula.plenr.feature.user.tempbusytimes.TempBusyTimesService

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

        var userPreferencesService: UserPreferencesService? = null

        registerService<UserPreferencesService> { ctx ->
            DatabaseUserPreferencesService(
                ctx,
                plenr.database,
                plenr.authService
            ).also { runBlocking { it.createIfNotExists() } }
                .also { userPreferencesService = it }
        }

        registerService<TrainingService> { ctx ->
            DatabaseTrainingService(
                ctx,
                plenr.database,
                serverUrl,
                plenr.authService,
                plenr.mailService,
                userPreferencesService!!
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
