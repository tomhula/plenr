package me.plenr.frontend.page.admin

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.form.check.checkBox
import dev.kilua.html.button
import dev.kilua.html.div
import dev.kilua.html.h2t
import dev.kilua.html.helpers.onClickLaunch
import dev.kilua.html.label
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.UserCreationFormState
import me.plenr.frontend.component.userCreationForm

@Composable
fun IComponent.addUserPage(viewModel: MainViewModel)
{
    val coroutineScope = rememberCoroutineScope()
    var formState by remember { mutableStateOf(UserCreationFormState()) }
    var isAdmin by remember { mutableStateOf(false) }

    h2t("Add User")

    userCreationForm(
        state = formState,
        onChange = { formState = it }
    )

    div {
        checkBox(isAdmin, id = "is-admin") {
            onChange {
                isAdmin = !isAdmin
            }
        }
        label {
           htmlFor("is-admin")
        }
    }


    button {
        +"Create User"
        onClickLaunch(coroutineScope) {
            viewModel.createUser(
                formState.toUserDto(isAdmin)
            )
        }
    }
}