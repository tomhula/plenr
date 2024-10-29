package me.tomasan7.plenr.config

import kotlinx.serialization.Serializable


@Serializable
data class Config(
    val server: Server = Server(),
    val database: Database = Database(),
    val smtp: Smtp = Smtp(),
    val passwordRequirements: PasswordRequirements = PasswordRequirements()
)
{
    @Serializable
    data class Server(
        val host: String = "localhost",
        val port: Int = 8080,
        val subPath: String = ""
    )

    @Serializable
    data class Database(
        val url: String = "jdbc:mysql://localhost:3306/plenr",
        val driver: String = "com.mysql.cj.jdbc.Driver",
        val user: String = "root",
        val password: String = ""
    )

    @Serializable
    data class PasswordRequirements(
        val requireLowercaseLetter: Boolean = false,
        val requireUppercaseLetter: Boolean = false,
        val requireNumber: Boolean = false,
        val requireSpecialSymbol: Boolean = false,
        val minLength: Int = 8
    )

    @Serializable
    data class Smtp(
        val host: String = "localhost",
        val port: Int = 1025,
        val username: String = "plenr",
        val password: String = ""
    )
}
