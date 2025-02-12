package cz.tomashula.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.onSubmit
import dev.kilua.core.IComponent
import dev.kilua.externals.console
import dev.kilua.form.*
import dev.kilua.form.text.text
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.flexPanel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun IComponent.adminSetupPage(plenrClient: MainViewModel)
{
    val router = Router.current
    val coroutineScope = rememberCoroutineScope()

    var submitted by remember { mutableStateOf(false) }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center,
        flexDirection = FlexDirection.Column,
    ) {
        h1t("Admin Setup")

        form<AdminSetupForm>(className = "needs-validation") {
            maxWidth(330.px)
            val validation by validationStateFlow.collectAsState()

            if (submitted)
                alert(
                    caption = "Admin account created",
                    content = "Check your email to set your password.",
                    callback = { router.navigate("/login") }
                )

            onSubmit {
                coroutineScope.launch {
                    val isValid = this@form.validate()
                    this@form.className = "was-validated"
                    if (isValid)
                    {
                        plenrClient.createUser(this@form.getData().toUserDto(true))
                        submitted = true
                    }
                }
            }

            div("mt-2") {
                fieldWithLabel("First Name", "form-label") {
                    text(required = true, id = it, className = "form-control") {
                        bind(AdminSetupForm::firstName)
                    }
                }
            }

            div("mt-2") {
                fieldWithLabel("Last Name", "form-label") {
                    text(required = true, id = it, className = "form-control") {
                        bind(AdminSetupForm::lastName)
                    }
                }
            }

            div("mt-2") {
                fieldWithLabel("Email", "form-label", wrapperClassName = "input-group") {
                    spant("@", className = "input-group-text", id = "inputGroupPrepend")
                    text(required = true, id = it, className = "form-control", type = InputType.Email) {
                        ariaDescribedby("inputGroupPrepend")
                        bind(AdminSetupForm::email)
                    }
                }
            }

            div("mt-2") {
                fieldWithLabel("Phone Number", "form-label") { inputId ->
                    text(required = true, id = inputId, className = "form-control", placeholder = "+000 000 000 000") {
                        bind(AdminSetupForm::phoneNumber) { it.value?.length == 16 }
                        maskOptions = ImaskOptions(pattern = PatternMask("{+}000{ }000{ }000{ }000"))
                    }
                    divt(className = "invalid-feedback", text = "Phone number must be in this format: +000 000 000 000")
                }
            }
            /* WrapperClassName makes the input and invalid-feedback wrapped, which is needed for the invalid-feedback to work */

            bsButton(label = "Create", type = ButtonType.Submit, className = "mt-2")
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
