package me.tomasan7.plenr.auth

import io.ktor.util.*
import me.tomasan7.plenr.feature.user.UserTable
import me.tomasan7.plenr.security.PasswordHasher
import me.tomasan7.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and

/** Checks tokens as a base64 encoded `username:password` combination. */
class BasicAuthService(
    database: Database,
    private val passwordHasher: PasswordHasher
) : AuthService, DatabaseService(database, UserTable)
{
    private suspend fun checkAuth(email: String, password: String): Int?
    {
        val passwordHash = passwordHasher.hash(password)

        return query {
            UserTable
                .select(UserTable.id)
                .where { UserTable.email eq email and (UserTable.passwordHash eq passwordHash) }
                .limit(1)
                .singleOrNull()
                ?.get(UserTable.id)?.value
        }
    }

    override suspend fun validateToken(token: String): Int?
    {
        val decodedToken = token.decodeBase64String()
        val (email, password) = decodedToken.split(":")

        return checkAuth(email, password)
    }

    override suspend fun authenticate(username: String, password: String): String?
    {
        return if (checkAuth(username, password) != null)
            "$username:$password".encodeBase64()
        else
            null
    }
}