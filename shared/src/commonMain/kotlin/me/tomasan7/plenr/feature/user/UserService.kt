package me.tomasan7.plenr.feature.user

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserService : RemoteService
{
    suspend fun createUser(user: UserDto): Int
    suspend fun getUser(id: Int): UserDto?
    suspend fun updateUser(user: UserDto): Boolean
    suspend fun deleteUser(id: Int): Boolean

    suspend fun adminExists(): Boolean
    suspend fun setPassword(token: ByteArray, password: String)
}