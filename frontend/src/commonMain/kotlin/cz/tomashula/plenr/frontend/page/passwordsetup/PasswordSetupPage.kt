package cz.tomashula.plenr.frontend.page.passwordsetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsForm
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.html.AlignItems
import dev.kilua.html.ButtonType
import dev.kilua.html.JustifyContent
import dev.kilua.html.bsButton
import dev.kilua.html.div
import dev.kilua.html.px
import dev.kilua.panel.hPanel
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import web.window

@Composable
fun IComponent.passwordSetupPage(mainViewModel: MainViewModel, token: String)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    val tokenUrlDecoded = token.decodeURLPart()

    hPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
    ) {
        bsForm<PasswordSetupForm>(
            onSubmit = { data, _, _ ->
                if (data.password == null || data.confirmPassword == null)
                {
                    window.alert("Passwords must be filled")
                    return@bsForm
                }
                if (data.password != data.confirmPassword)
                {
                    window.alert("Passwords do not match")
                    return@bsForm
                }

                coroutineScope.launch {
                    mainViewModel.setPassword(tokenUrlDecoded, data.password)
                    router.navigate(Route.LOGIN)
                }
            }
        ) {
            maxWidth(330.px)

            div("mt-2") {
                bsLabelledFormField("Password") {
                    bsFormInput(it, bindKey = PasswordSetupForm::password, type = InputType.Password)
                }
            }
            div("mt-2") {
                bsLabelledFormField("Confirm password") {
                    bsFormInput(it, bindKey = PasswordSetupForm::confirmPassword, type = InputType.Password)
                }
            }

            bsButton("Set Password", type = ButtonType.Submit, className = "mt-3")
        }
    }
}

@Serializable
private data class PasswordSetupForm(
    val password: String? = null,
    val confirmPassword: String? = null
)
