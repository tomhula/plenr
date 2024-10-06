package me.tomasan7.plenr.config

import kotlinx.serialization.Serializable


@Serializable
data class Config(
    val server: Server = Server(),
    val database: Database = Database()
)
{
    @Serializable
    data class Server(
        val host: String = "localhost",
        val port: Int = 8080
    )

    @Serializable
    data class Database(
        val url: String = "jdbc:mysql://localhost:3306/plenr",
        val driver: String = "com.mysql.jdbc.Driver",
        val user: String = "root",
        val password: String = ""
    )
}
