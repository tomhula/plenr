package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("user") {
    val firstName = varchar("first_name", 20)
    val lastName = varchar("last_name", 20)
    val email = varchar("email", 30).uniqueIndex()
    val phone = char("phone", 16)
    val passwordHash = binary("password_hash", 32).nullable()
    val isAdmin = bool("is_admin")
}
