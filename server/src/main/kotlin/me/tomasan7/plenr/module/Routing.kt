package me.tomasan7.plenr.module

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import me.tomasan7.plenr.Plenr
import me.tomasan7.plenr.routing.api.apiRoute
import kotlin.io.path.Path

fun Application.configureRouting(plenr: Plenr, subPath: String = "")
{
    routing {
        if (developmentMode)
            sourceFiles()

        route(subPath) {
            apiRoute(plenr)
            singlePageApplication {
                useResources = true
                filesPath = "/frontend/"
                defaultPage = "index.html"
            }
        }
    }
}

private fun Route.sourceFiles()
{
    staticFiles("/", Path("/home/tomas/projects/plenr/frontend").toFile())
    staticFiles("/", Path("/home/tomas/projects/plenr").toFile())
}