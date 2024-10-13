package me.tomasan7.plenr.config

/**
 * A [ConfigManager] that reads environment variables to override the primary [ConfigManager].
 * Overrides the server host and port.
 */
class EnvVarConfigManager(
    private val primaryConfigManager: ConfigManager
) : ConfigManager by primaryConfigManager
{
    override suspend fun getConfig(): Config
    {
        val jsonConfig = primaryConfigManager.getConfig()
        val serverHost = System.getenv(EnvVars.SERVER_HOST) ?: jsonConfig.server.host
        val serverPort = System.getenv(EnvVars.SERVER_PORT)?.toIntOrNull() ?: jsonConfig.server.port
        val serverConfig = Config.Server(serverHost, serverPort)
        val finalConfig = jsonConfig.copy(server = serverConfig)
        return finalConfig
    }

    private companion object
    {
        private object EnvVars
        {
            const val SERVER_HOST = "PLENR_SERVER_HOST"
            const val SERVER_PORT = "PLENR_SERVER_PORT"
        }
    }
}