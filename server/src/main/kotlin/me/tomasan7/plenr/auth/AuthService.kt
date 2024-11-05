package me.tomasan7.plenr.auth

import me.tomasan7.plenr.feature.user.AuthenticationResponse
import me.tomasan7.plenr.feature.user.UserDto

/** Is responsible for handling user authentication */
interface AuthService
{
    /** Validates [token] and returns the authenticated user, or `null`, if they are invalid. */
    suspend fun validateToken(token: String): UserDto?

    /** Checks [username] and [password] and returns an auth token if they are valid. */
    suspend fun authenticate(username: String, password: String): AuthenticationResponse?
}