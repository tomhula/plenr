package cz.tomashula.plenr

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.Krpc
import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.BasicAuthService
import cz.tomashula.plenr.config.Config
import cz.tomashula.plenr.config.ConfigManager
import cz.tomashula.plenr.config.EnvVarConfigManager
import cz.tomashula.plenr.config.JsonFileConfigManager
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.mail.SmtpMailService
import cz.tomashula.plenr.module.configureContentNegotiation
import cz.tomashula.plenr.module.configureExceptionHandling
import cz.tomashula.plenr.module.configureRouting
import cz.tomashula.plenr.security.*
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
    internal lateinit var authService: AuthService

    lateinit var mailService: MailService

    fun init(configFilePath: Path, mock: Boolean)
    {
        runBlocking {
            /* Config has to be initialized first */
            initConfigManager(configFilePath)
            reloadConfig()

            initPasswordManagement()
            initApplicationEngine()
            initDb()
            initServices()

            if (mock)
            {
                // TODO: Replace with logger
                println("Filling with mock data!")
                Mocker(database, passwordHasher).fill()
            }
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
        authService = BasicAuthService(database, passwordHasher)
    }

    fun startBlocking()
    {
        embeddedServer.start(wait = true)
    }

    private fun Application.module()
    {
        install(Krpc)
        configureContentNegotiation()
        configureExceptionHandling()
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
