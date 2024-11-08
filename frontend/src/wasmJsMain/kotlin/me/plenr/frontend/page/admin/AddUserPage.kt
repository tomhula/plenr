package me.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import app.softwork.routingcompose.navigate
import dev.kilua.core.IComponent
import dev.kilua.form.check.checkBox
import dev.kilua.form.form
import dev.kilua.html.*
import dev.kilua.html.helpers.onClickLaunch
import kotlinx.browser.window
import kotlinx.coroutines.launch
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.UserCreationFormState
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.onSubmit
import me.plenr.frontend.component.userCreationFormFields

@Composable
fun IComponent.addUserPage(viewModel: MainViewModel)
{
    val router = Router.current
    val coroutineScope = rememberCoroutineScope()
    var formState by remember { mutableStateOf(UserCreationFormState()) }
    var isAdmin by remember { mutableStateOf(false) }

    form {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        onSubmit {
            coroutineScope.launch {
                viewModel.createUser(
                    formState.toUserDto(isAdmin)
                )
                window.alert("User created")
                router.navigate("/manage-users")
            }
        }

        h2t("Add User")

        userCreationFormFields(
            state = formState,
            onChange = { formState = it }
        )

        div(className = "form-field") {
            checkBox(isAdmin, id = "is-admin", className = "form-field-input") {
                onChange {
                    isAdmin = !isAdmin
                }
            }
            label(htmlFor = "is-admin-input", className = "form-field-label") {
                +"Admin"
            }
        }

        button(label = "Create User", type = ButtonType.Submit)
    }
}