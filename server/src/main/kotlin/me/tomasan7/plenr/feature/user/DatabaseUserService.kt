package me.tomasan7.plenr.feature.user

import io.ktor.http.*
import io.ktor.util.*
import me.tomasan7.plenr.auth.AuthService
import me.tomasan7.plenr.auth.UnauthorizedException
import me.tomasan7.plenr.mail.MailService
import me.tomasan7.plenr.security.PasswordHasher
import me.tomasan7.plenr.security.PasswordValidator
import me.tomasan7.plenr.security.TokenGenerator
import me.tomasan7.plenr.service.DatabaseService
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
    private val authService: AuthService
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

        val userId = query {
            val userId = UserTable.insertAndGetId {
                it[name] = newUser.name
                it[email] = newUser.email
                it[phone] = newUser.phone
                it[passwordHash] = null
                it[isAdmin] = newUser.isAdmin
            }.value

            UserActivationTable.insert {
                it[UserActivationTable.userId] = userId
                it[UserActivationTable.token] = token
            }

            userId
        }

        val token = tokenGenerator.generate(32)

        val tokenB64 = token.encodeBase64()
        val tokenB64UrlEncoded = tokenB64.encodeURLPath(encodeSlash = true)

        mailService.sendMail(
            recipient = newUser.email,
            subject = "Welcome to Plenr",
            body = "Welcome to Plenr! Set your password here: $serverUrl/set-password/${tokenB64UrlEncoded}"
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