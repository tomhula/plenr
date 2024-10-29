package me.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.html.ButtonType
import dev.kilua.html.button
import dev.kilua.html.h1t
import kotlinx.coroutines.launch
import me.plenr.frontend.PlenrClient
import me.plenr.frontend.component.userCreationForm

@Composable
fun IComponent.adminSetupPage(plenrClient: PlenrClient)
{
    var state by remember { mutableStateOf(AdminSetupState()) }
    val coroutineScope = rememberCoroutineScope()

    h1t("Admin Setup")

    userCreationForm(state.userCreationFormState) {
        state = AdminSetupState(userCreationFormState = it)
    }

    button(className = "submit-button") {
        type(ButtonType.Submit)
        +"Submit"

        onClick {
            coroutineScope.launch {
                val id = plenrClient.createUser(state.userCreationFormState.toUserDto())
                println("Check your email $id")
            }
        }
    }
}