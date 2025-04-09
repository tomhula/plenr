package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsInvalidFeedback
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import cz.tomashula.plenr.frontend.component.bsValidatedForm
import dev.kilua.core.IComponent
import dev.kilua.form.ImaskOptions
import dev.kilua.form.InputType
import dev.kilua.form.PatternMask
import dev.kilua.form.check.checkBox
import dev.kilua.form.fieldWithLabel
import dev.kilua.html.*
import dev.kilua.modal.alert
import dev.kilua.panel.flexPanel
import kotlinx.serialization.Serializable

@Composable
fun IComponent.addUserPage(viewModel: MainViewModel)
{
    val router = Router.current

    val done by remember { mutableStateOf(false) }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center
    ) {
        bsValidatedForm<UserCreationForm>(
            onSubmitValid = { data ->
                viewModel.createUser(data.toUserDto())
                router.navigate(Route.MANAGE_USERS)
            }
        ) {
            if (done)
                alert(
                    caption = "User created",
                    content = "User was created successfully and will receive an activation email.",
                    callback = { router.navigate(Route.MANAGE_USERS) }
                )

            bsLabelledFormField("First name", groupClassName = "mt-2") {
                bsFormInput(it, UserCreationForm::firstName)
            }

            bsLabelledFormField("Last name", groupClassName = "mt-2") {
                bsFormInput(it, UserCreationForm::lastName)
            }

            bsLabelledFormField("Email", groupClassName = "mt-2") {
                bsFormInput(it, UserCreationForm::email, type = InputType.Email)
            }

            bsLabelledFormField("Phone Number") { inputId ->
                bsFormInput(
                    id = inputId,
                    bindKey = UserCreationForm::phone,
                    validator = { it.value?.length == 16 },
                    placeholder = "+000 000 000 000"
                ) {
                    maskOptions = ImaskOptions(pattern = PatternMask("{+}000{ }000{ }000{ }000"))
                }
                bsInvalidFeedback("Phone number must be in this format: +000 000 000 000")
            }

            div(className = "form-check mt-2") {
                fieldWithLabel("Admin", className = "form-check-label") {
                    checkBox(id = it, className = "form-check-input") {
                        bind(UserCreationForm::isAdmin)
                    }
                }
            }

            bsButton(label = "Create User", type = ButtonType.Submit, className = "mt-3")
        }
    }
}

@Serializable
private data class UserCreationForm(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isAdmin: Boolean = false
)
{
    fun toUserDto() = UserDto(
        id = -1,
        firstName = firstName!!,
        lastName = lastName!!,
        email = email!!,
        phone = phone!!,
        isAdmin = isAdmin!!,
        isActive = false
    )
}
