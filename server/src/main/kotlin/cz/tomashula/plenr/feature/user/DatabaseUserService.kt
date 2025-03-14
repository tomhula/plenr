package cz.tomashula.plenr.feature.user

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.security.PasswordHasher
import cz.tomashula.plenr.security.PasswordValidator
import cz.tomashula.plenr.security.TokenGenerator
import cz.tomashula.plenr.service.DatabaseService
import cz.tomashula.plenr.util.now
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
) : UserService, DatabaseService(database, UserTable, UserSetPasswordTable)
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

            val passwordToken = setUserPasswordRequest(userId, reset = false)

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

    private suspend fun Transaction.setUserPasswordRequest(userId: Int, reset: Boolean): ByteArray
    {
        val activationToken = tokenGenerator.generate(32)

        UserSetPasswordTable.insert {
            it[UserSetPasswordTable.userId] = userId
            it[UserSetPasswordTable.token] = activationToken
            it[UserSetPasswordTable.reset] = reset
            it[UserSetPasswordTable.issuedAt] = LocalDateTime.now()
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

            setUserPasswordRequest(userId, true)
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
            UserTable.join(UserSetPasswordTable, JoinType.INNER).update({ UserSetPasswordTable.token eq token }) {
                it[UserTable.passwordHash] = passwordHash
            }
            UserSetPasswordTable.deleteWhere { UserSetPasswordTable.token eq token }
        }
    }

    override suspend fun login(username: String, password: String): AuthenticationResponse?
    {
        return authService.authenticate(username, password)
    }

    override suspend fun updateUser(user: UserDto, authToken: String): Boolean
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can update users")

        return dbQuery {
            UserTable.update({ UserTable.id eq user.id }) {
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[email] = user.email
                it[phone] = user.phone
                it[isAdmin] = user.isAdmin
                // Note: isActive is a computed property based on whether passwordHash is null
            } > 0
        }
    }

    override suspend fun deleteUser(id: Int, authToken: String): Boolean
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can delete users")

        if (caller.id == id)
            throw IllegalArgumentException("Cannot delete your own account")

        return dbQuery {
            UserTable.deleteWhere { UserTable.id eq id } > 0
        }
    }
}
