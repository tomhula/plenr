package cz.tomashula.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.*
import dev.kilua.KiluaScope
import dev.kilua.core.IComponent
import dev.kilua.form.ImaskOptions
import dev.kilua.form.InputType
import dev.kilua.form.PatternMask
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.flexPanel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

@Composable
fun IComponent.adminSetupPage(plenrClient: MainViewModel)
{
    val router = Router.current

    var submitted by remember { mutableStateOf(false) }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
        flexDirection = FlexDirection.Column,
    ) {
        if (submitted)
            alert(
                caption = "Admin account created",
                content = "Check your email to set your password.",
                callback = { router.navigate(Route.LOGIN) }
            )

        bsValidatedForm<AdminSetupForm>(
            onSubmitValid = { form ->
                plenrClient.createUser(form.toUserDto(true))
                submitted = true
            }
        ) {
            maxWidth(330.px)

            div("mt-2") {
                bsLabelledFormField("First Name") {
                    bsFormInput(it, AdminSetupForm::firstName)
                }
            }

            div("mt-2") {
                bsLabelledFormField("Last Name") {
                    bsFormInput(id = it, AdminSetupForm::lastName)
                }
            }

            div("mt-2") {
                bsLabelledFormField("Email") {
                    bsFormInput(it, AdminSetupForm::email, type = InputType.Email) {
                        ariaDescribedby("inputGroupPrepend")
                    }
                }
            }

            div("mt-2") {
                bsLabelledFormField("Phone Number") { inputId ->
                    bsFormInput(
                        id = inputId,
                        bindKey = AdminSetupForm::phoneNumber,
                        validator = { it.value?.length == 16 },
                        placeholder = "+000 000 000 000"
                    ) {
                        maskOptions = ImaskOptions(pattern = PatternMask("{+}000{ }000{ }000{ }000"))
                    }
                    bsInvalidFeedback("Phone number must be in this format: +000 000 000 000")
                }
            }

            bsButton(label = "Create", type = ButtonType.Submit, className = "mt-3")

            // Dynamic validation
            stateFlow.onEach {
                this.validate()
            }.launchIn(KiluaScope)
        }
    }
}

@Serializable
private data class AdminSetupForm(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)

private fun AdminSetupForm.toUserDto(isAdmin: Boolean) = UserDto(
    id = -1,
    firstName = firstName!!,
    lastName = lastName!!,
    email = email!!,
    phone = phoneNumber!!,
    isActive = false,
    isAdmin = isAdmin
)
