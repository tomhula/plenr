package cz.tomashula.plenr.feature.user

import io.ktor.http.*
import io.ktor.util.*
import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.security.PasswordHasher
import cz.tomashula.plenr.security.PasswordValidator
import cz.tomashula.plenr.security.TokenGenerator
import cz.tomashula.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.*
import kotlin.coroutines.CoroutineContext

class DatabaseUserService(
    override val coroutineContext: CoroutineContext,
    serverUrl: String,
    database: Database,
    private val passwordValidator: PasswordValidator,
    private val passwordHasher: PasswordHasher,
    private val tokenGenerator: TokenGenerator,
    private val mailService: MailService,
    private val authService: cz.tomashula.plenr.auth.AuthService
) : UserService, DatabaseService(database, UserTable, UserActivationTable)
{
    private val serverUrl = serverUrl.removeSuffix("/")

    override suspend fun createUser(newUser: UserDto, authToken: String?): Int
    {
        val userAuthenticated = authToken == null

        if (userAuthenticated)
        {
            val creatingAdminAccount = !newUser.isAdmin

            if (creatingAdminAccount || adminExists())
                throw UnauthorizedException("Only admins can create new users")
        }
        else
        {
            val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

            if (!caller.isAdmin)
                throw UnauthorizedException("Only admins can create new users")
        }

        val activationToken = tokenGenerator.generate(32)

        val userId = query {
            val userId = UserTable.insertAndGetId {
                it[firstName] = newUser.firstName
                it[lastName] = newUser.lastName
                it[email] = newUser.email
                it[phone] = newUser.phone
                it[passwordHash] = null
                it[isAdmin] = newUser.isAdmin
            }.value

            UserActivationTable.insert {
                it[UserActivationTable.userId] = userId
                it[UserActivationTable.token] = activationToken
            }

            userId
        }


        val activationTokenB64 = activationToken.encodeBase64()
        val activationTokenB64UrlEncoded = activationTokenB64.encodeURLPathPart()

        mailService.sendMail(
            recipient = newUser.email,
            subject = "Welcome to Plenr",
            body = "Welcome to Plenr! Set your password here: $serverUrl/set-password/${activationTokenB64UrlEncoded}"
        )

        return userId
    }

    override suspend fun getUser(id: Int, authToken: String): UserDto?
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != id && !caller.isAdmin)
            throw UnauthorizedException("You can only view your own user data")

        return query { UserTable.selectAll().where { UserTable.id eq id }.singleOrNull() }?.toUserDto()
    }

    override suspend fun getAllUsers(authToken: String): List<UserDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can view all users")

        return query { UserTable.selectAll().map { it.toUserDto() } }
    }

    override suspend fun adminExists(): Boolean
    {
        return query {
            UserTable.select(UserTable.id).where { UserTable.isAdmin eq true }.limit(1).singleOrNull()
        } != null
    }

    override suspend fun setPassword(token: ByteArray, password: String)
    {
        check(token.size == 32) { "Invalid token size" }
        check(passwordValidator.validate(password).isEmpty()) { "Invalid password" }

        val passwordHash = passwordHasher.hash(password)
        query {
            UserTable.join(UserActivationTable, JoinType.INNER).update({ UserActivationTable.token eq token }) {
                it[UserTable.passwordHash] = passwordHash
            }
        }
    }

    override suspend fun login(username: String, password: String): AuthenticationResponse?
    {
        return authService.authenticate(username, password)
    }
}
