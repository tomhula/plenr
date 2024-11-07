package me.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.html.*
import me.plenr.frontend.MainViewModel
import me.tomasan7.plenr.feature.user.UserDto

@Composable
fun IComponent.manageUsersPage(viewModel: MainViewModel)
{
    val router = Router.current
    var allUsersExceptMe: List<UserDto>? by remember { mutableStateOf(listOf()) }

    h2t("Manage Users")

    LaunchedEffect(Unit) {
        allUsersExceptMe = viewModel.getAllUsers() - viewModel.user!!
    }

    pt("Users:")
    if (allUsersExceptMe == null)
    {
        pt("Loading users...")
        return
    }

    ul {
        allUsersExceptMe!!.forEach { user ->
            li {
                +user.firstName
                +" "
                +user.lastName
            }
        }
    }

    button {
        +"Add User"
        onClick {
            router.navigate("+/add-user")
        }
    }
}