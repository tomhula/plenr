package me.tomasan7.plenr.util

import io.ktor.server.plugins.*
import io.ktor.server.routing.*

fun RoutingContext.requireParam(parameterName: String) =
    getParam(parameterName) ?: throw MissingRequestParameterException(parameterName)


fun RoutingContext.getParam(parameterName: String) = call.parameters[parameterName]