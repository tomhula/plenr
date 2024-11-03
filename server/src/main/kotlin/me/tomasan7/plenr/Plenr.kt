package me.tomasan7.plenr

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.RPC
import me.tomasan7.plenr.config.Config
import me.tomasan7.plenr.config.ConfigManager
import me.tomasan7.plenr.config.EnvVarConfigManager
import me.tomasan7.plenr.config.JsonFileConfigManager
import me.tomasan7.plenr.mail.MailService
import me.tomasan7.plenr.mail.SmtpMailService
import me.tomasan7.plenr.module.*
import me.tomasan7.plenr.security.*
import org.jetbrains.exposed.sql.Database
import java.nio.file.Path

class Plenr : ConfigManager
{
    private lateinit var embeddedServer: EmbeddedServer<*, *>
    private lateinit var configManager: ConfigManager
    private lateinit var config: Config
    internal lateinit var database: Database
    internal lateinit var passwordValidator: PasswordValidator
    internal lateinit var passwordHasher: PasswordHasher
    internal lateinit var tokenGenerator: TokenGenerator

    lateinit var mailService: MailService

    fun init(configFilePath: Path)
    {
        runBlocking {
            /* Config has to be initialized first */
            initConfigManager(configFilePath)
            reloadConfig()

            initPasswordManagement()
            initApplicationEngine()
            initDb()
            initServices()
        }
    }

    private suspend fun initConfigManager(configFilePath: Path)
    {
        configManager = EnvVarConfigManager(JsonFileConfigManager(configFilePath))
        configManager.initConfig()
    }

    private fun initPasswordManagement()
    {
        passwordValidator = PasswordValidator(
            lowerCaseLetter = config.passwordRequirements.requireLowercaseLetter,
            upperCaseLetter = config.passwordRequirements.requireUppercaseLetter,
            number = config.passwordRequirements.requireNumber,
            specialSymbol = config.passwordRequirements.requireSpecialSymbol,
            minLength = config.passwordRequirements.minLength
        )
        passwordHasher = Sha256PasswordHasher()
        tokenGenerator = SecureRandomTokenGenerator()
    }

    private fun initApplicationEngine()
    {
        embeddedServer = embeddedServer(
            factory = Netty,
            host = config.server.host,
            port = config.server.port,
            module = { module() }
        )
    }

    private fun initDb()
    {
        val dbConfig = config.database
        database = Database.connect(
            url = dbConfig.url,
            driver = dbConfig.driver,
            user = dbConfig.user,
            password = dbConfig.password
        )
    }

    private suspend fun initServices()
    {
        mailService = SmtpMailService(
            smtpHost = config.smtp.host,
            smtpPort = config.smtp.port,
            smtpUsername = config.smtp.username,
            smtpPassword = config.smtp.password
        )
    }

    fun startBlocking()
    {
        embeddedServer.start(wait = true)
    }

    private fun Application.module()
    {
        install(RPC)
        configureContentNegotiation()
        configureExceptionHandling()
        configureAuthentication()
        configureRouting(this@Plenr, Url(config.server.url).fullPath.removeSuffix("/"))
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