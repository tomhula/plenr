package me.tomasan7.plenr.feature.user

import io.ktor.http.*
import io.ktor.util.*
import me.tomasan7.plenr.auth.AuthService
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

    override suspend fun getUser(id: Int, authToken: String): UserDto?
    {
        return query { UserTable.selectAll().where { UserTable.id eq id }.singleOrNull() }?.toUserDto()
    }

    override suspend fun createUser(user: UserDto, authToken: String?): Int
    {
        val token = tokenGenerator.generate(32)

        val userId = query {
            val userId = UserTable.insertAndGetId {
                it[name] = user.name
                it[email] = user.email
                it[phone] = user.phone
                it[passwordHash] = null
                it[isAdmin] = user.isAdmin
            }.value

            UserActivationTable.insert {
                it[UserActivationTable.userId] = userId
                it[UserActivationTable.token] = token
            }

            userId
        }

        val tokenB64 = token.encodeBase64()
        val tokenB64UrlEncoded = tokenB64.encodeURLPath(encodeSlash = true)

        mailService.sendMail(
            recipient = user.email,
            subject = "Welcome to Plenr",
            body = "Welcome to Plenr! Set your password here: $serverUrl/set-password/${tokenB64UrlEncoded}"
        )

        return userId
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

    override suspend fun login(username: String, password: String): String?
    {
        return authService.authenticate(username, password)
    }
}