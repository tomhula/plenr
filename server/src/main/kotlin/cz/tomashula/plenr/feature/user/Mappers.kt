package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUserDto(
    alias: Alias<UserTable>? = null,
    id: Int = alias?.let { this[it[UserTable.id]].value } ?: this[UserTable.id].value,
    firstName: String = alias?.let { this[it[UserTable.firstName]] } ?: this[UserTable.firstName],
    lastName: String = alias?.let { this[it[UserTable.lastName]] } ?: this[UserTable.lastName],
    email: String = alias?.let { this[it[UserTable.email]] } ?: this[UserTable.email],
    phone: String = alias?.let { this[it[UserTable.phone]] } ?: this[UserTable.phone],
    isAdmin: Boolean = alias?.let { this[it[UserTable.isAdmin]] } ?: this[UserTable.isAdmin],
    isActive: Boolean = alias?.let { this[it[UserTable.passwordHash]] != null } ?: (this[UserTable.passwordHash] != null)
) = UserDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    isAdmin = isAdmin,
    isActive = isActive
)
