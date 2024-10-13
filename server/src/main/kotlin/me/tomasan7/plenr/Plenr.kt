package me.tomasan7.plenr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import me.tomasan7.Greeting
import me.tomasan7.plenr.config.Config
import me.tomasan7.plenr.config.ConfigManager
import me.tomasan7.plenr.config.EnvVarConfigManager
import me.tomasan7.plenr.config.JsonFileConfigManager
import java.nio.file.Path

class Plenr : ConfigManager
{
    private lateinit var applicationEngine: ApplicationEngine
    private lateinit var configManager: ConfigManager
    private lateinit var config: Config

    fun init(configFilePath: Path)
    {
        runBlocking {
            initConfigManager(configFilePath)
            reloadConfig()
            initApplicationEngine()
        }
    }

    fun startBlocking()
    {
        applicationEngine.start(wait = true)
    }

    private suspend fun initConfigManager(configFilePath: Path)
    {
        configManager = EnvVarConfigManager(JsonFileConfigManager(configFilePath))
        configManager.initConfig()
    }

    private fun initApplicationEngine()
    {
        applicationEngine = embeddedServer(
            factory = Netty,
            host = config.server.host,
            port = config.server.port,
            module = { module() }
        )
    }

    private fun Application.module()
    {
        routing {
            get("/") {
                call.respondText("Ktor: ${Greeting().greet()}")
            }
        }
    }

    override suspend fun getConfig() = config

    override suspend fun setConfig(config: Config)
    {
        this.config = config
        configManager.setConfig(config)
    }

    suspend fun reloadConfig()
    {
        config = configManager.getConfig()
    }
}