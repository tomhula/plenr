package cz.tomashula.plenr.security

interface PasswordHasher
{
    suspend fun hash(password: String): ByteArray
    suspend fun verify(password: String, hash: ByteArray): Boolean
    suspend fun getHashSizeInBytes(): Int
}
