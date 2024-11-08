package me.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import app.softwork.routingcompose.navigate
import dev.kilua.core.IComponent
import dev.kilua.form.form
import dev.kilua.html.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.onSubmit
import me.plenr.frontend.component.userCreationFormFields

@Composable
fun IComponent.adminSetupPage(plenrClient: MainViewModel)
{
    val router = Router.current
    var state by remember { mutableStateOf(AdminSetupState()) }
    val coroutineScope = rememberCoroutineScope()

    form {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)
        onSubmit {
            coroutineScope.launch {
                val id = plenrClient.createUser(state.userCreationFormState.toUserDto(isAdmin = true))
                window.alert("Check your email")
                router.navigate("/login")
            }
        }

        h1t("Admin Setup")

        userCreationFormFields(state.userCreationFormState) {
            state = AdminSetupState(userCreationFormState = it)
        }

        button("Create", className = "submit-button", type = ButtonType.Submit)
    }
}