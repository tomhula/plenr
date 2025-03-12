package cz.tomashula.plenr.feature.user

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserService : RemoteService
{
    /**
     * Allows admins to create new users.
     * Allows creation of admin account without authentication if one does not exist yet.
     */
    suspend fun createUser(newUser: UserDto, authToken: String?): Int

    /**
     * Allows admins to get any user.
     * Allows each user to get themselves.
     */
    suspend fun getUser(id: Int, authToken: String): UserDto?

    /** Allows admins to get all users. */
    suspend fun getAllUsers(authToken: String): List<UserDto>

    /** Allows admins to update a user. */
    suspend fun updateUser(user: UserDto, authToken: String): Boolean

    /** Allows admins to delete a user. */
    suspend fun deleteUser(id: Int, authToken: String): Boolean

    /** Allows anyone to check if an admin account exists. */
    suspend fun adminExists(): Boolean

    /** Allows anyone (even unauthenticated users) to request a password reset. */
    suspend fun requestPasswordReset(email: String)

    /** Sets a password based on a [password reset token][token]. */
    suspend fun setPassword(token: ByteArray, password: String)

    /** Checks [username] and [password] and returns the authenticated [user][UserDto] with auth token. */
    suspend fun login(username: String, password: String): AuthenticationResponse?
}
