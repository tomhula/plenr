package cz.tomashula.plenr.module

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import cz.tomashula.plenr.Plenr
import cz.tomashula.plenr.routing.api.apiRoute
import kotlin.io.path.Path

fun Application.configureRouting(plenr: Plenr, subPath: String = "")
{
    routing {
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