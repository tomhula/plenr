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
import cz.tomashula.plenr.util.now
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import kotlin.coroutines.CoroutineContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DatabaseUserService(
    override val coroutineContext: CoroutineContext,
    serverUrl: String,
    database: Database,
    private val passwordValidator: PasswordValidator,
    private val passwordHasher: PasswordHasher,
    private val tokenGenerator: TokenGenerator,
    private val mailService: MailService,
    private val authService: AuthService
) : UserService, DatabaseService(database, UserTable, UserSetPassword)
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

        val (userId, passwordToken) = dbQuery {
            val userId = UserTable.insertAndGetId {
                it[firstName] = newUser.firstName
                it[lastName] = newUser.lastName
                it[email] = newUser.email
                it[phone] = newUser.phone
                it[passwordHash] = null
                it[isAdmin] = newUser.isAdmin
            }.value

            val passwordToken = runBlocking {
                setUserPasswordRequest(userId, reset = false)
            }

            userId to passwordToken
        }

        val passwordTokenB64 = passwordToken.encodeBase64()
        val passwordTokenB64UrlEncoded = passwordTokenB64.encodeURLPathPart()

        mailService.sendMail(
            recipient = newUser.email,
            subject = "Welcome to Plenr",
            body = "Welcome to Plenr! Set your password here: $serverUrl/set-password/${passwordTokenB64UrlEncoded}"
        )

        return userId
    }

    private suspend fun setUserPasswordRequest(userId: Int, reset: Boolean): ByteArray
    {
        val activationToken = tokenGenerator.generate(32)

        dbQuery {
            UserSetPassword.insert {
                it[UserSetPassword.userId] = userId
                it[UserSetPassword.token] = activationToken
                it[UserSetPassword.reset] = reset
                it[UserSetPassword.issuedAt] = LocalDateTime.now()
            }
        }

        return activationToken
    }

    override suspend fun getUser(id: Int, authToken: String): UserDto?
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != id && !caller.isAdmin)
            throw UnauthorizedException("You can only view your own user data")

        return dbQuery { UserTable.selectAll().where { UserTable.id eq id }.singleOrNull() }?.toUserDto()
    }

    override suspend fun getAllUsers(authToken: String): List<UserDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can view all users")

        return dbQuery { UserTable.selectAll().map { it.toUserDto() } }
    }

    override suspend fun adminExists(): Boolean
    {
        return dbQuery {
            UserTable.select(UserTable.id).where { UserTable.isAdmin eq true }.limit(1).singleOrNull()
        } != null
    }

    override suspend fun requestPasswordReset(email: String)
    {
        val passwordResetToken = dbQuery {
            val userId = UserTable
                .select(UserTable.id)
                .where { UserTable.email eq email }
                .limit(1)
                .singleOrNull()
                ?.get(UserTable.id)
                ?.value ?: return@dbQuery null

            runBlocking {
                setUserPasswordRequest(userId, true)
            }
        }

        if (passwordResetToken != null)
        {
            val passwordResetTokenB64 = passwordResetToken.encodeBase64()
            val passwordResetTokenB64UrlEncoded = passwordResetTokenB64.encodeURLPathPart()

            mailService.sendMail(
                recipient = email,
                subject = "Plenr password reset request",
                body = "To reset your password, click here: $serverUrl/set-password/${passwordResetTokenB64UrlEncoded}"
            )
        }
    }

    override suspend fun setPassword(token: ByteArray, password: String)
    {
        check(token.size == 32) { "Invalid token size" }
        check(passwordValidator.validate(password).isEmpty()) { "Invalid password" }

        val passwordHash = passwordHasher.hash(password)
        dbQuery {
            UserTable.join(UserSetPassword, JoinType.INNER).update({ UserSetPassword.token eq token }) {
                it[UserTable.passwordHash] = passwordHash
            }
            UserSetPassword.deleteWhere { UserSetPassword.token eq token }
        }
    }

    override suspend fun login(username: String, password: String): AuthenticationResponse?
    {
        return authService.authenticate(username, password)
    }
}
