package me.tomasan7.plenr.module

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import me.tomasan7.plenr.Plenr
import me.tomasan7.plenr.routing.api.apiRoute

fun Application.configureRouting(plenr: Plenr, subPath: String = "")
{
    routing {
        route(subPath) {
            apiRoute(plenr.userService)
            singlePageApplication {
                useResources = true
                filesPath = "/"
                defaultPage = "index.html"
            }
        }
    }
}