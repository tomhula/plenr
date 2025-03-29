package cz.tomashula.plenr.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.KebabCaseParamMapper
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.hocon.HoconParser
import com.sksamuel.hoplite.resolver.context.EnvVarContextResolver
import com.sksamuel.hoplite.resolver.context.ReferenceContextResolver
import com.sksamuel.hoplite.sources.EnvironmentVariableOverridePropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

/**
 * Provides [Config] from a HOCON file on the filesystem on path [path].
 * When [path] does not exist a default config is copied from [defaultConfigResourcePath] in resources.
 */
class FileConfigProvider(
    private val path: String,
    private val defaultConfigResourcePath: String
) : ConfigProvider
{
    @OptIn(ExperimentalHoplite::class)
    override fun getConfig(): Config
    {
        createFileIfNotExists()

        return ConfigLoaderBuilder.default()
            .addParser("conf", HoconParser())
            .addFileSource(File(path))
            .addPropertySource(EnvironmentVariableOverridePropertySource(true))
            .withExplicitSealedTypes()
            .removePreprocessors()
            // https://github.com/sksamuel/hoplite/issues/474
            // .addResolver(PortValidator())
            .addResolver(EnvVarContextResolver)
            .addResolver(ReferenceContextResolver)
            .addParameterMapper(KebabCaseParamMapper)
            .build()
            .loadConfigOrThrow()
    }

    private fun createFileIfNotExists(): Boolean
    {
        val defaultConfigResource = this::class.java.getResource(defaultConfigResourcePath)
            ?: throw IllegalStateException("Default config resource not found")

        val file = File(path)

        if (!file.exists())
        {
            logger.info { "Creating default config file at $path" }

            defaultConfigResource.openStream().use { defaultConfigInputStream ->
                Files.copy(defaultConfigInputStream, file.toPath())
            }

            return true
        }

        return false
    }
}