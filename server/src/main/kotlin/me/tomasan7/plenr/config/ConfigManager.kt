package me.tomasan7.plenr.config

interface ConfigManager
{
    suspend fun loadConfig(): Config
    suspend fun storeConfig(config: Config)
}