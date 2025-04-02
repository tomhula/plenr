package cz.tomashula.plenr.frontend.page.login

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.html.*
import kotlinx.coroutines.launch
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.*
import dev.kilua.form.text.password
import dev.kilua.html.helpers.TagStyleFun.Companion.textDecoration
import dev.kilua.html.helpers.onClickLaunch
import dev.kilua.html.style.pClass
import dev.kilua.html.style.style
import dev.kilua.panel.flexPanel
import kotlinx.serialization.Serializable
import dev.kilua.KiluaScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        bsForm<LoginForm>(
            onSubmit = { data, form, _ ->
                authenticated = mainViewModel.login(data.email!!, data.password!!)
                if (authenticated == true)
                    router.navigate(Route.HOME)
                form.validate()
                form.className = "was-validated"
            }
        ) {
            maxWidth(330.px)

            div("mt-2") {
                bsLabelledFormField("Email") {
                    bsFormInput(it, LoginForm::email, type = InputType.Email, validator = { 
                        authenticated != false 
                    })
                }
            }

            div("mt-2") {
                bsLabelledFormField("Password") {
                    bsFormInput(it, LoginForm::password, type = InputType.Password, validator = { 
                        authenticated != false 
                    })
                }
                if (authenticated == false) {
                    bsInvalidFeedback("Incorrect email or password")
                }
            }

            navLink(Route.FORGOT_PASSWORD, "Forgot password?") {
                style {
                    display(Display.Block)
                    color(Color.Gray)
                    fontSize(0.8.rem)
                    textDecoration(TextDecorationLine.None)
                    pClass("hover") {
                        textDecoration(TextDecorationLine.Underline)
                    }
                }
            }

            bsButton("Login", type = ButtonType.Submit, className = "mt-3")

            // Dynamic validation
            stateFlow.onEach {
                this.className = this.className?.replace("was-validated", "")
            }.launchIn(KiluaScope)
        }
    }
}

@Serializable
private data class LoginForm(
    val email: String? = null,
    val password: String? = null
)
