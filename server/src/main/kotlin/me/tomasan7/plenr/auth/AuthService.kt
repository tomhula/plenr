package me.tomasan7.plenr.auth

/** Is responsible for handling user authentication */
interface AuthService
{
    /** Validates [token] and returns the authenticated user's id, or `null`, if they are invalid. */
    suspend fun validateToken(token: String): Int?

    /** Checks [username] and [password] and returns an auth token if they are valid. */
    suspend fun authenticate(username: String, password: String): String?
}