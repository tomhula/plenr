package me.tomasan7.plenr.feature.user

import me.tomasan7.plenr.api.UserDto
import me.tomasan7.plenr.mail.MailService
import me.tomasan7.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class DatabaseUserService(
    database: Database,
    private val mailService: MailService
) : UserService, DatabaseService(database, UserTable)
{
    private fun ResultRow.toUserDto() = UserDto(
        id = this[UserTable.id].value,
        name = this[UserTable.name],
        email = this[UserTable.email],
        phone = this[UserTable.phone],
        isAdmin = this[UserTable.isAdmin],
        isActive = this[UserTable.passwordHash] != null
    )

    override suspend fun getUser(id: Int): UserDto?
    {
        return query { UserTable.selectAll().where { UserTable.id eq id }.singleOrNull() }?.toUserDto()
    }

    override suspend fun createUser(user: UserDto): Int
    {
        val id = query {
            UserTable.insertAndGetId {
                it[name] = user.name
                it[email] = user.email
                it[phone] = user.phone
                it[passwordHash] = null
                it[isAdmin] = user.isAdmin
            }
        }.value

        mailService.sendMail(
            recipient = user.email,
            subject = "Welcome to Plenr",
            body = "Welcome to Plenr! Your user ID is $id"
        )

        return id
    }

    override suspend fun updateUser(user: UserDto): Boolean
    {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(id: Int): Boolean
    {
        TODO("Not yet implemented")
    }

    override suspend fun adminExists(): Boolean
    {
        return query {
            UserTable.select(UserTable.id).where { UserTable.isAdmin eq true }.limit(1).singleOrNull()
        } != null
    }
}