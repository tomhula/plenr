package cz.tomashula.plenr.config

/**
 * A [ConfigManager] that reads environment variables to override the primary [ConfigManager].
 * Overrides server.host, server.port, server.url, database.url, database.user, database.password, smtp.host, smtp.port, smtp.username, smtp.password.
 */
class EnvVarConfigManager(
    private val primaryConfigManager: ConfigManager
) : ConfigManager by primaryConfigManager
{
    override suspend fun getConfig(): Config
    {
        val primaryConfig = primaryConfigManager.getConfig()
        val server = overrideServer(primaryConfig.server)
        val database = overrideDatabase(primaryConfig.database)
        val smtp = overrideSmtp(primaryConfig.smtp)
        return primaryConfig.copy(server = server, database = database, smtp = smtp)
    }

    private fun overrideServer(primary: Config.Server): Config.Server
    {
        val host = EnvVars.SERVER_HOST.get() ?: primary.host
        val port = EnvVars.SERVER_PORT.get()?.toIntOrNull() ?: primary.port
        val url = EnvVars.SERVER_URL.get() ?: primary.url
        return primary.copy(host = host, port = port, url = url)
    }

    private fun overrideDatabase(primary: Config.Database): Config.Database
    {
        val url = EnvVars.DATABASE_URL.get() ?: primary.url
        val user = EnvVars.DATABASE_USER.get() ?: primary.user
        val password = EnvVars.DATABASE_PASSWORD.get() ?: primary.password
        return primary.copy(url = url, user = user, password = password)
    }

    private fun overrideSmtp(primary: Config.Smtp): Config.Smtp
    {
        val host = EnvVars.SMTP_HOST.get() ?: primary.host
        val port = EnvVars.SMTP_PORT.get()?.toIntOrNull() ?: primary.port
        val username = EnvVars.SMTP_USERNAME.get() ?: primary.username
        val password = EnvVars.SMTP_PASSWORD.get() ?: primary.password
        return primary.copy(host = host, port = port, username = username, password = password)
    }

    private fun String.get() = System.getenv(this)

    private companion object
    {
        private object EnvVars
        {
            const val SERVER_HOST = "PLENR_SERVER_HOST"
            const val SERVER_PORT = "PLENR_SERVER_PORT"
            const val SERVER_URL = "PLENR_SERVER_URL"

            const val DATABASE_URL = "PLENR_DATABASE_URL"
            const val DATABASE_USER = "PLENR_DATABASE_USER"
            const val DATABASE_PASSWORD = "PLENR_DATABASE_PASSWORD"

            const val SMTP_HOST = "PLENR_SMTP_HOST"
            const val SMTP_PORT = "PLENR_SMTP_PORT"
            const val SMTP_USERNAME = "PLENR_SMTP_USERNAME"
            const val SMTP_PASSWORD = "PLENR_SMTP_PASSWORD"
        }
    }
}
