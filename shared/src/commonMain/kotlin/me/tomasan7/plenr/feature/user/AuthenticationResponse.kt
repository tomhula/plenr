package me.tomasan7.plenr.feature.user

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationResponse(
    val user: UserDto,
    val authToken: String
)
