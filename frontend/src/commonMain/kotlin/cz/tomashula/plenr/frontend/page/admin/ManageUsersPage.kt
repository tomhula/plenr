package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.applyColumn
import cz.tomashula.plenr.frontend.component.outlinedMaterialIconButton
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.panel.gridPanel
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import web.window

@Composable
fun IComponent.manageUsersPage(viewModel: MainViewModel)
{
    val router = Router.current
    var allUsersExceptMe: List<UserDto>? by remember { mutableStateOf(listOf()) }

    LaunchedEffect(Unit) {
        allUsersExceptMe = viewModel.getAllUsers() - viewModel.user!!
    }

    div {
        applyColumn()
        rowGap(10.px)

        if (allUsersExceptMe == null)
        {
            pt("Loading users...")
            return@div
        }

        gridPanel(
            rowGap = 10.px,
            columnGap = 10.px,
            gridTemplateColumns = "repeat(auto-fill, 300px)"
        ) {
            width(100.perc)
            allUsersExceptMe!!.forEach { user ->
                manageUserCard(
                    user = user,
                    onEditClick = { window.alert("TODO: Edit clicked") },
                    onDeleteClick = { window.alert("TODO: Delete clicked") },
                    onPasswordResetClick = { window.alert("TODO: Password Reset clicked") }
                )
            }
        }

        bsButton("Add User", style = ButtonStyle.BtnPrimary) {
            onClick { router.navigate(Route.ADD_USER) }
        }
    }
}

@Composable
fun IComponent.manageUserCard(
    user: UserDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPasswordResetClick: () -> Unit
)
{
    div("card") {
        div("card-body") {
            hPanel(
                className = "card-title",
                justifyContent = JustifyContent.SpaceBetween
            ) {
                +user.fullName

                div {
                    outlinedMaterialIconButton("lock_reset", onClick = onPasswordResetClick)
                    outlinedMaterialIconButton("edit", onClick = onEditClick)
                    outlinedMaterialIconButton("delete", onClick = onDeleteClick, color = Color("#ce4949"))
                }
            }
            vPanel(
                className = "card-text text-body-secondary",
                gap = 5.px
            ) {
                spant(user.email)
                spant(user.phone)
            }
        }
    }
}
