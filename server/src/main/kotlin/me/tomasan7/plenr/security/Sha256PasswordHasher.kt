package me.tomasan7.plenr.security

import java.security.MessageDigest

class Sha256PasswordHasher : PasswordHasher
{
    private val messageDigest = MessageDigest.getInstance("SHA-256")

    override suspend fun hash(password: String): ByteArray
    {
        return messageDigest.digest(password.toByteArray())
    }

    override suspend fun verify(password: String, hash: ByteArray): Boolean
    {
        return hash.contentEquals(hash(password))
    }

    override suspend fun getHashSizeInBytes() = messageDigest.digestLength
}