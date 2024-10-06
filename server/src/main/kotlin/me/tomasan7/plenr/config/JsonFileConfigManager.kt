package me.tomasan7.plenr.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class JsonFileConfigManager(
    private val filePath: Path
) : ConfigManager
{
    private val json = Json

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun loadConfig(): Config
    {
        val stream = filePath.inputStream()
        val config = json.decodeFromStream<Config>(stream)

        return config
    }

    override suspend fun storeConfig(config: Config)
    {
        val stream = filePath.outputStream()
        json.encodeToStream(config, stream)
    }
}