package me.tomasan7.plenr.feature.user

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserService : RemoteService
{
    /**
     * Allows admins to create new users.
     * Allows creation of admin account without authentication if one does not exist yet.
     */
    suspend fun createUser(user: UserDto, authToken: String?): Int

    /**
     * Allows admins to get any user.
     * Allows each user to get themselves.
     */
    suspend fun getUser(id: Int, authToken: String): UserDto?

    /** Allows anyone to check if an admin account exists. */
    suspend fun adminExists(): Boolean

    suspend fun setPassword(token: ByteArray, password: String)

    /** Checks [username] and [password] and returns an auth token. */
    suspend fun login(username: String, password: String): String?
}