package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable

object UserActivationTable : IntIdTable("user_activation")
{
    val userId = reference("user_id", UserTable.id)
    val token = binary("token", 32).uniqueIndex()
}
