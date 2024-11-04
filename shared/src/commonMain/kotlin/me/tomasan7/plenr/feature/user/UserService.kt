package me.tomasan7.plenr.feature.user

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserService : RemoteService
{
    suspend fun createUser(user: UserDto, authToken: String): Int
    suspend fun getUser(id: Int, authToken: String): UserDto?
    suspend fun updateUser(user: UserDto, authToken: String): Boolean
    suspend fun deleteUser(id: Int, authToken: String): Boolean

    suspend fun adminExists(): Boolean
    suspend fun setPassword(token: ByteArray, password: String)

    /** Checks [username] and [password] and returns an auth token. */
    suspend fun login(username: String, password: String): String?
}