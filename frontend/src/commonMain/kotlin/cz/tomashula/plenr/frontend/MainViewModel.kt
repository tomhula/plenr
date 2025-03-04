package cz.tomashula.plenr.frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cz.tomashula.plenr.feature.training.CreateOrUpdateTrainingDto
import cz.tomashula.plenr.feature.training.TrainingService
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.UserService
import cz.tomashula.plenr.feature.user.preferences.PermanentBusyTimesDto
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesDto
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesService
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.feature.user.tempbusytimes.TempBusyTimesService
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.datetime.LocalDateTime
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json
import web.localStorage
import web.window

private const val AUTH_TOKEN_STORAGE_KEY = "authToken"
private const val USER_STORAGE_KEY = "user"

class MainViewModel
{
    private val httpClient = HttpClient(Js) {
        /*defaultRequest {
            url(window.origin + "/api/")
        }*/
        /*install(ContentNegotiation) {
            json()
        }*/
        installKrpc()
    }
    private val json = Json
    private lateinit var userService: UserService
    private lateinit var trainingService: TrainingService
    private lateinit var preferencesService: UserPreferencesService
    private lateinit var tempBusyTimesService: TempBusyTimesService
    private var authToken: String? = null
    var user: UserDto? by mutableStateOf(null)

    val isLoggedIn: Boolean
        get() = authToken != null && user != null

    suspend fun init()
    {
        authToken = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
        user = localStorage.getItem(USER_STORAGE_KEY)?.let { json.decodeFromString(it) }

        val ktorRpcClient = httpClient.rpc {
            url {
                host = window.location.hostname
                port = getCurrentPort().let { if (it == 3000) 8080 else it } /* Webpack dev server detection */
                protocol = if (window.location.protocol == "https:") URLProtocol.WSS else URLProtocol.WS
                encodedPath = "/api"
            }

            rpcConfig {
                serialization {
                    json(json)
                }
            }
        }
        userService = ktorRpcClient.withService()
        trainingService = ktorRpcClient.withService()
        preferencesService = ktorRpcClient.withService()
        tempBusyTimesService = ktorRpcClient.withService()
    }

    fun logout()
    {
        authToken = null
        user = null
        localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
        localStorage.removeItem(USER_STORAGE_KEY)
    }

    suspend fun adminExists() = userService.adminExists()

    suspend fun createUser(user: UserDto) = userService.createUser(user, authToken)

    suspend fun setPassword(tokenB64: String, password: String)
    {
        val token = tokenB64.decodeBase64Bytes()
        userService.setPassword(token, password)
    }

    suspend fun login(username: String, password: String): Boolean
    {
        val authResponse = userService.login(username, password) ?: return false

        authToken = authResponse.authToken
        user = authResponse.user
        localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, authToken!!)
        localStorage.setItem(USER_STORAGE_KEY, json.encodeToString(user))

        return true
    }

    suspend fun getAllUsers(): List<UserDto>
    {
        if (user?.isAdmin != true)
            return emptyList()
        if (authToken == null)
            return emptyList()

        return userService.getAllUsers(authToken!!)
    }

    suspend fun arrangeTrainings(trainings: Set<CreateOrUpdateTrainingDto>)
    {
        trainingService.arrangeTrainings(trainings, authToken!!)
    }

    suspend fun getAllTrainingsAdmin(
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): List<TrainingWithParticipantsDto>
    {
        if (authToken == null)
            return emptyList()
        else if (user?.isAdmin != true)
            return emptyList()

        return trainingService.getAllTrainings(from, to, authToken!!)
    }

    suspend fun getMyTrainings(
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): List<TrainingWithParticipantsDto>
    {
        if (authToken == null)
            return emptyList()

        return trainingService.getTrainingsForUser(user!!.id, from, to, authToken!!)
    }

    suspend fun getPreferences() = preferencesService.getUserPreferences(user!!.id, authToken!!) ?: UserPreferencesDto(
        1,
        false, false,
        false, false,
        false, false,
    )

    suspend fun requestPasswordReset(email: String) = userService.requestPasswordReset(email)

    suspend fun setPreferences(preferences: UserPreferencesDto) = preferencesService.setUserPreferences(user!!.id, preferences, authToken!!)

    suspend fun getPermanentBusyTimes() = preferencesService.getPermanentBusyTimes(user!!.id, authToken!!).busyTimes

    suspend fun getPermanentBusyTimesAdmin(userId: Int) = preferencesService.getPermanentBusyTimes(userId, authToken!!).busyTimes

    suspend fun setPermanentBusyTimes(busyTimes: WeeklyTimeRanges) = preferencesService.setPermanentBusyTimes(
        PermanentBusyTimesDto(user!!.id, busyTimes),
        authToken!!
    )
}
