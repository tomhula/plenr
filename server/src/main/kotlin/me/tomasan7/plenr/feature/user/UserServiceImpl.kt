package me.tomasan7.plenr.feature.user

import me.tomasan7.plenr.api.UserDto
import me.tomasan7.plenr.data.UserTable
import me.tomasan7.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class DatabaseUserService(database: Database) : UserService, DatabaseService(database, UserTable)
{
    private fun ResultRow.toUserDto() = UserDto(
        id = this[UserTable.id].value,
        name = this[UserTable.name],
        email = this[UserTable.email],
        phone = this[UserTable.phone],
        isAdmin = this[UserTable.isAdmin]
    )

    override suspend fun getUser(id: Int): UserDto?
    {
        return query { UserTable.selectAll().where { UserTable.id eq id }.singleOrNull() }?.toUserDto()
    }

    override suspend fun createUser(user: UserDto): Int
    {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: UserDto): Boolean
    {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(id: Int): Boolean
    {
        TODO("Not yet implemented")
    }
}