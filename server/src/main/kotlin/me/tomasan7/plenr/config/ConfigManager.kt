package me.tomasan7.plenr.config

interface ConfigManager
{
    /**
     * Used for example to store default config if it does not exist yet.
     */
    suspend fun initConfig() {}
    suspend fun getConfig(): Config
    suspend fun setConfig(config: Config)
}
