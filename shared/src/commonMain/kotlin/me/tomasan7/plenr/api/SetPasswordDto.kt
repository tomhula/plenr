package me.tomasan7.plenr.api

import kotlinx.serialization.Serializable

@Serializable
data class SetPasswordDto(
    val token: ByteArray,
    val password: String
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is SetPasswordDto) return false

        if (!token.contentEquals(other.token)) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = token.contentHashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}
