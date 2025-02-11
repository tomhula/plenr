package cz.tomashula.plenr.security

/** Generates a random byte sequence */
interface TokenGenerator
{
    suspend fun generate(byteSize: Int): ByteArray
}
