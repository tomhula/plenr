package me.tomasan7.plenr.config


data class Config(
    val server: Server,
    val database: Database
)
{
    data class Server(
        val host: String,
        val port: Int
    )

    data class Database(
        val url: String,
        val driver: String,
        val user: String,
        val password: String
    )
}
