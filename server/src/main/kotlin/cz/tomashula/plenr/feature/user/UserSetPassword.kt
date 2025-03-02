package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UserSetPassword : IntIdTable("user_set_password")
{
    val userId = reference("user_id", UserTable.id)
    val token = binary("token", 32).uniqueIndex()
    val reset = bool("reset")
    val issuedAt = datetime("issued_at")
}
