package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.*
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.gridPanel
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import dev.kilua.toast.ToastPosition
import dev.kilua.toast.toast
import kotlinx.coroutines.launch

@Composable
fun IComponent.manageUsersPage(viewModel: MainViewModel)
{
    val router = Router.current
    var allUsersExceptMe: List<UserDto>? by remember { mutableStateOf(listOf()) }
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var editDialogShown by remember { mutableStateOf(false) }
    var deleteDialogShown by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(Unit) {
        refreshUserList(viewModel) { users -> allUsersExceptMe = users }
    }

    // Edit dialog
    selectedUser?.let { user ->
        userEditDialog(
            shown = editDialogShown,
            user = user,
            onSave = { updatedUser ->
                coroutineScope.launch {
                    val success = viewModel.updateUser(updatedUser)
                    if (success) {
                        refreshUserList(viewModel) { users -> allUsersExceptMe = users }
                        toast("User updated successfully", position = ToastPosition.BottomCenter)
                    } else
                        toast("Failed to update user", position = ToastPosition.BottomCenter)
                    editDialogShown = false
                }
            },
            onDismiss = { editDialogShown = false }
        )
    }

    // Delete dialog
    selectedUser?.let { user ->
        userDeleteDialog(
            shown = deleteDialogShown,
            user = user,
            onConfirm = {
                coroutineScope.launch {
                    val success = viewModel.deleteUser(user.id)
                    if (success) {
                        refreshUserList(viewModel) { users -> allUsersExceptMe = users }
                        toast("User deleted successfully", position = ToastPosition.BottomCenter)
                    } else
                        toast("Failed to delete user", position = ToastPosition.BottomCenter)
                    deleteDialogShown = false
                }
            },
            onDismiss = { deleteDialogShown = false }
        )
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
                    onEditClick = { 
                        selectedUser = user
                        editDialogShown = true
                    },
                    onDeleteClick = { 
                        selectedUser = user
                        deleteDialogShown = true
                    }
                )
            }
        }

        bsButton("Add User", style = ButtonStyle.BtnPrimary) {
            onClick { router.navigate(Route.ADD_USER) }
        }
    }
}

private suspend fun refreshUserList(viewModel: MainViewModel, onResult: (List<UserDto>) -> Unit) {
    val users = viewModel.getAllUsers() - viewModel.user!!
    onResult(users)
}

@Composable
fun IComponent.manageUserCard(
    user: UserDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
