package me.tomasan7.plenr.auth

import io.ktor.util.*
import me.tomasan7.plenr.feature.user.AuthenticationResponse
import me.tomasan7.plenr.feature.user.UserDto
import me.tomasan7.plenr.feature.user.UserTable
import me.tomasan7.plenr.feature.user.toUserDto
import me.tomasan7.plenr.security.PasswordHasher
import me.tomasan7.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

/** Checks tokens as a base64 encoded `username:password` combination. */
class BasicAuthService(
    database: Database,
    private val passwordHasher: PasswordHasher
) : AuthService, DatabaseService(database, UserTable)
{
    private suspend fun checkAuth(email: String, password: String): UserDto?
    {
        val passwordHash = passwordHasher.hash(password)

        return query {
            UserTable
                .selectAll()
                .where { UserTable.email eq email and (UserTable.passwordHash eq passwordHash) }
                .limit(1)
                .singleOrNull()
                ?.toUserDto()
        }
    }

    override suspend fun validateToken(token: String): UserDto?
    {
        val decodedToken = token.decodeBase64String()
        val (email, password) = decodedToken.split(":")

        return checkAuth(email, password)
    }

    override suspend fun authenticate(username: String, password: String): AuthenticationResponse?
    {
        val user = checkAuth(username, password) ?: return null
        val authToken = "$username:$password".encodeBase64()

        return AuthenticationResponse(user, authToken)
    }
}