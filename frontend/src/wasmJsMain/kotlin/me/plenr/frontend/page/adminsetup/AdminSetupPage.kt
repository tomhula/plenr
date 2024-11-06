package me.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.html.*
import kotlinx.coroutines.launch
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.userCreationForm

@Composable
fun IComponent.adminSetupPage(plenrClient: MainViewModel)
{
    var state by remember { mutableStateOf(AdminSetupState()) }
    val coroutineScope = rememberCoroutineScope()

    div {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        h1t("Admin Setup")

        userCreationForm(state.userCreationFormState) {
            state = AdminSetupState(userCreationFormState = it)
        }

        button(className = "submit-button") {
            type(ButtonType.Submit)
            +"Submit"

            onClick {
                coroutineScope.launch {
                    val id = plenrClient.createUser(state.userCreationFormState.toUserDto(isAdmin = true))
                    println("Check your email $id")
                }
            }
        }
    }
}