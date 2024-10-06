package me.tomasan7.plenr.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class JsonFileConfigManager(
    private val filePath: Path
) : ConfigManager
{
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    override suspend fun initConfig()
    {
        if (!filePath.exists())
            setConfig(Config())
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getConfig(): Config
    {
        return filePath.inputStream().use { json.decodeFromStream<Config>(it) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun setConfig(config: Config)
    {
        filePath.outputStream().use { json.encodeToStream(config, it) }
    }
}