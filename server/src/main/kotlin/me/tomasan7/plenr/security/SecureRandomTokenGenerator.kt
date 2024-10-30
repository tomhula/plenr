package me.tomasan7.plenr.security

import java.security.SecureRandom

class SecureRandomTokenGenerator : TokenGenerator
{
    private val random = SecureRandom()

    override suspend fun generate(byteSize: Int): ByteArray
    {
        val token = ByteArray(byteSize)
        random.nextBytes(token)
        return token
    }
}