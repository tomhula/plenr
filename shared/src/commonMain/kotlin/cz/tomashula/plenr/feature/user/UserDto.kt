package cz.tomashula.plenr.feature.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val isActive: Boolean,
    val isAdmin: Boolean,
)
{
    val fullName = "$firstName $lastName"
}
