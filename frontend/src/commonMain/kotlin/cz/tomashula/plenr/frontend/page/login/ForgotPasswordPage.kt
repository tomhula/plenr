package cz.tomashula.plenr.frontend.page.login

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsForm
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.flexPanel
import kotlinx.serialization.Serializable

@Composable
fun IComponent.forgotPasswordPage(
    viewModel: MainViewModel
)
{
    val router = Router.current

    var submitted by remember { mutableStateOf(false) }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
        flexDirection = FlexDirection.Column,
    ) {
        bsForm<ForgotPasswordForm>(
            onSubmit = { data, _, _ ->
                viewModel.requestPasswordReset(data.email!!)
                submitted = true
            }
        ) {
            if (submitted)
                alert(
                    caption = "Password reset email sent",
                    content = "Check your email to reset the password.",
                    callback = { router.navigate(Route.LOGIN) }
                )

            bsLabelledFormField("Email") {
                bsFormInput(it, ForgotPasswordForm::email)
            }

            bsButton(type = ButtonType.Submit, label = "Submit", className = "mt-3")
        }
    }
}

@Serializable
private data class ForgotPasswordForm(
    val email: String? = null
)
