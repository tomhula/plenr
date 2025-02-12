package cz.tomashula.plenr.frontend.page.login

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.html.*
import kotlinx.coroutines.launch
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.*
import dev.kilua.panel.flexPanel
import kotlinx.serialization.Serializable

@Composable
fun IComponent.loginPage(mainViewModel: MainViewModel)
{
    val router = Router.current
    var authenticated by remember { mutableStateOf<Boolean?>(null) }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
        flexDirection = FlexDirection.Column,
    ) {
        h1t("Login", className = "mb-5 mt-5") {
            textAlign(TextAlign.Center)
        }

        bsForm<LoginForm>(
            onSubmit = { data, _, _ ->
                authenticated = mainViewModel.login(data.email!!, data.password!!)
                if (authenticated == true)
                    router.navigate("/")
            }
        ) {
            div("mt-2") {
                bsLabelledFormField("Email") {
                    bsFormInput(it, LoginForm::email, type = InputType.Email)
                }
            }

            div("mt-2") {
                bsLabelledFormField("Password") {
                    bsFormInput(it, LoginForm::password, type = InputType.Password)
                }
            }

            bsButton("Login", type = ButtonType.Submit, className = "mt-3")

            when (authenticated)
            {
                false -> p { +"Incorrect credentials" }
                else -> Unit
            }
        }
    }
}

@Serializable
private data class LoginForm(
    val email: String? = null,
    val password: String? = null
)
