package cz.tomashula.plenr

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import kotlin.io.path.Path

fun main(args: Array<String>)
{
    Command().main(args)
}

class Command : CliktCommand()
{
    private val configFilePath: Path by option().path(
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true,
        mustBeWritable = true
    ).default(Path("config.json")).help("Path to the config that will be used.")

    override fun run()
    {
        val plenr = Plenr()
        plenr.init(configFilePath)
        plenr.startBlocking()
    }
}
