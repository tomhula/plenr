package cz.tomashula.plenr.frontend.page.passwordsetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsInvalidFeedback
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import cz.tomashula.plenr.frontend.component.bsValidatedForm
import dev.kilua.KiluaScope
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.hPanel
import io.ktor.http.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun IComponent.passwordSetupPage(mainViewModel: MainViewModel, token: String)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    val tokenUrlDecoded = token.decodeURLPart()

    var submitted by remember { mutableStateOf(false) }

    hPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
    ) {
        bsValidatedForm<PasswordSetupForm>(
            onSubmitValid = { data ->
                coroutineScope.launch {
                    mainViewModel.setPassword(tokenUrlDecoded, data.password!!)
                    submitted = true
                }
            }
        ) {
            if (submitted)
                alert(
                    caption = "Password set",
                    content = "Password set, you can now login to your account",
                    okTitle = "Go to login",
                    callback = { router.navigate(Route.LOGIN) }
                )
            
            maxWidth(330.px)

            div("mt-2") {
                bsLabelledFormField("Password") {
                    bsFormInput(it, bindKey = PasswordSetupForm::password, type = InputType.Password)
                }
            }
            div("mt-2") {
                bsLabelledFormField("Confirm password") {
                    bsFormInput(it, bindKey = PasswordSetupForm::confirmPassword, type = InputType.Password, validator = {
                        return@bsFormInput it.value == this@bsValidatedForm.getData().password
                    })
                }
                bsInvalidFeedback("Passwords do not match")
            }

            bsButton("Set Password", type = ButtonType.Submit, className = "mt-3")

            stateFlow.onEach {
                this.validate()
            }.launchIn(KiluaScope)
        }
    }
}

@Serializable
private data class PasswordSetupForm(
    val password: String? = null,
    val confirmPassword: String? = null
)
