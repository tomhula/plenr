package cz.tomashula.plenr.frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cz.tomashula.plenr.feature.training.CreateOrUpdateTrainingDto
import cz.tomashula.plenr.feature.training.TrainingService
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.UserService
import cz.tomashula.plenr.feature.user.availability.BusyPeriodDto
import cz.tomashula.plenr.feature.user.availability.UserAvailabilityService
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesDto
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesService
import cz.tomashula.plenr.feature.user.preferences.UserRegularAvailabilityDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.util.LocalDateTimePeriod
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json

class AppViewModel : ViewModel()
{
    private val httpClient = HttpClient {
        installKrpc()
    }
    private val json = Json
    private val authStorage = createAuthStorage()
    private lateinit var userService: UserService
    private lateinit var trainingService: TrainingService
    private lateinit var preferencesService: UserPreferencesService
    private lateinit var userAvailabilityService: UserAvailabilityService
    private var authToken: String? = null
    var user: UserDto? by mutableStateOf(null)

    val isLoggedIn: Boolean
        get() = authToken != null && user != null

    suspend fun init()
    {
        authToken = authStorage.getAuthToken()
        user = authStorage.getUser()

        val ktorRpcClient = httpClient.rpc {
            url {
                host = getServerAddress()
                port = getServerPort()
                protocol = getServerProtocol()
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
        userAvailabilityService = ktorRpcClient.withService()
    }

    fun logout()
    {
        authToken = null
        user = null
        authStorage.clear()
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
        authStorage.storeAuth(authToken!!, user!!)

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

    suspend fun getUserRegularAvailability() = userAvailabilityService.getUserRegularAvailability(user!!.id, authToken!!).availableTimes

    suspend fun getUserRegularAvailabilityAdmin(userId: Int) = userAvailabilityService.getUserRegularAvailability(userId, authToken!!).availableTimes

    suspend fun getUsersAvailabilityForDay(userIds: List<Int>, day: LocalDate) =
        userAvailabilityService.getUsersAvailabilityForDay(
            userIds, day, authToken!!
        )

    suspend fun setUserRegularAvailability(busyTimes: WeeklyTimeRanges) = userAvailabilityService.setUserRegularAvailability(
        UserRegularAvailabilityDto(user!!.id, busyTimes),
        authToken!!
    )

    suspend fun updateUser(user: UserDto): Boolean {
        if (authToken == null || this.user?.isAdmin != true)
            return false

        return userService.updateUser(user, authToken!!)
    }

    suspend fun deleteUser(userId: Int): Boolean {
        if (authToken == null || user?.isAdmin != true)
            return false

        return userService.deleteUser(userId, authToken!!)
    }

    suspend fun getUserBusyPeriods(from: LocalDateTime? = null, to: LocalDateTime? = null): List<BusyPeriodDto> {
        if (authToken == null || user == null)
            return emptyList()

        return userAvailabilityService.getBusyPeriodsForUser(user!!.id, from, to, authToken!!)
    }

    suspend fun addBusyPeriod(start: LocalDateTime, end: LocalDateTime): Int? {
        if (authToken == null || user == null)
            return null

        return userAvailabilityService.addBusyPeriod(
            LocalDateTimePeriod(start, end),
            authToken!!
        )
    }

    suspend fun removeBusyPeriod(busyPeriodId: Int): Boolean {
        if (authToken == null || user == null)
            return false

        userAvailabilityService.removeBusyPeriod(busyPeriodId, authToken!!)
        return true
    }
}
