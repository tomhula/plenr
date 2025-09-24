package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.datetime

object UserSetPasswordTable : IntIdTable("user_set_password")
{
    val userId = reference("user_id", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val token = binary("token", 32).uniqueIndex()
    val reset = bool("reset")
    val issuedAt = datetime("issued_at")
}
