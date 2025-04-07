package cz.tomashula.plenr.routing.api

import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import cz.tomashula.plenr.Plenr
import cz.tomashula.plenr.feature.training.DatabaseTrainingService
import cz.tomashula.plenr.feature.training.TrainingService
import cz.tomashula.plenr.feature.user.DatabaseUserAvailabilityService
import cz.tomashula.plenr.feature.user.DatabaseUserPreferencesService
import cz.tomashula.plenr.feature.user.DatabaseUserService
import cz.tomashula.plenr.feature.user.UserService
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesService
import cz.tomashula.plenr.feature.user.availability.UserAvailabilityService

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

        /* This Service usage is NOT correct. See https://github.com/Kotlin/kotlinx-rpc/issues/102#issuecomment-2723966393 */
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

        registerService<UserPreferencesService> { ctx ->
            DatabaseUserPreferencesService(
                ctx,
                plenr.database,
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


        registerService<UserAvailabilityService> { ctx ->
            DatabaseUserAvailabilityService(
                ctx,
                plenr.database,
                plenr.authService
            ).also { runBlocking { it.createIfNotExists() } }
        }
    }
}
