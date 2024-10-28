package me.tomasan7.plenr.api

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val isAdmin: Boolean,
)
