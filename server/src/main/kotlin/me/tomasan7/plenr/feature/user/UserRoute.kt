package me.tomasan7.plenr.feature.user

import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.tomasan7.plenr.api.UserDto
import me.tomasan7.plenr.util.requireParam

fun Route.userRoute(userService: UserService)
{
    route("/user")
    {
        get {
            call.respondText("TODO: List of users")
        }

        get("/{id}")
        {
            val id = requireParam("id").toInt()
            val user = userService.getUser(id) ?: throw NotFoundException("User not found")

            call.respond(user)
        }

        post {
            val user = call.receive<UserDto>()
            val id = userService.createUser(user)
            call.respond(id)
        }
    }

    get("/admin-exists")
    {
        call.respond(userService.adminExists())
    }
}