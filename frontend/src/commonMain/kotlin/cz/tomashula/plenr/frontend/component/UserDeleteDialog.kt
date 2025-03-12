package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.*
import cz.tomashula.plenr.feature.user.UserDto
import dev.kilua.core.IComponent
import dev.kilua.html.*

@Composable
fun IComponent.userDeleteDialog(
    shown: Boolean,
    user: UserDto,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    bsModalDialog(
        shown = shown,
        title = "Delete User",
        onDismiss = onDismiss,
        footer = {
            bsButton("Cancel", style = ButtonStyle.BtnSecondary) {
                onClick { onDismiss() }
            }
            bsButton("Delete", style = ButtonStyle.BtnDanger) {
                onClick { onConfirm() }
            }
        }
    ) {
        p {
            +"Are you sure you want to delete user ${user.fullName}?"
        }
        p {
            +"This action cannot be undone."
        }
    }
}
