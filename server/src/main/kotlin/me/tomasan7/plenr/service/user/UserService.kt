package me.tomasan7.plenr.service.user

import me.tomasan7.plenr.api.UserDto

interface UserService
{
    suspend fun createUser(user: UserDto): Int
    suspend fun getUser(id: Int): UserDto?
    suspend fun updateUser(user: UserDto): Boolean
    suspend fun deleteUser(id: Int): Boolean
}