package me.tomasan7.plenr.routing.api

import io.ktor.server.routing.*
import me.tomasan7.plenr.routing.api.user.userRoute
import me.tomasan7.plenr.service.user.UserService

fun Routing.apiRoute(
    userService: UserService
)
{
    route("/api")
    {
        userRoute(userService)
    }
}