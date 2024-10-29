package me.tomasan7.plenr.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptionHandling()
{
    install(StatusPages)
    {
        exception<MissingRequestParameterException> { call, cause ->
            call.respondText(status = HttpStatusCode.BadRequest) { cause.message ?: "Missing request parameter." }
        }

        exception<NotFoundException> { call, cause ->
            call.respondText(status = HttpStatusCode.NotFound) { cause.message ?: "Not found." }
        }
    }
}