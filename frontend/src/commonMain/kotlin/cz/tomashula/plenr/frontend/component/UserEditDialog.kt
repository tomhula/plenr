package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.*
import cz.tomashula.plenr.feature.user.UserDto
import dev.kilua.core.IComponent
import dev.kilua.form.Form
import dev.kilua.form.InputType
import dev.kilua.form.check.checkBox
import dev.kilua.form.fieldWithLabel
import dev.kilua.html.*
import kotlinx.serialization.Serializable

@Composable
fun IComponent.userEditDialog(
    shown: Boolean,
    user: UserDto,
    onSave: (UserDto) -> Unit,
    onDismiss: () -> Unit
) {
    var form: Form<UserEditForm>? = null

    bsModalDialog(
        shown = shown,
        title = "Edit User",
        onDismiss = onDismiss,
        footer = {
            bsButton("Cancel", style = ButtonStyle.BtnSecondary) {
                onClick { onDismiss() }
            }
            bsButton("Save", style = ButtonStyle.BtnPrimary) {
                onClick {
                    form?.let {
                        onSave(it.getData().toUserDto(user.id, user.isActive))
                    }
                }
            }
        }
    ) {
        form = bsFormRef<UserEditForm>(
            onSubmit = { data, _, _ ->
                onSave(data.toUserDto(user.id, user.isActive))
            }
        ) {
            LaunchedEffect(user) {
                setData(user.toUserEditForm())
            }

            bsLabelledFormField("First name", groupClassName = "mt-2") {
                bsFormInput(it, UserEditForm::firstName)
            }

            bsLabelledFormField("Last name", groupClassName = "mt-2") {
                bsFormInput(it, UserEditForm::lastName)
            }

            bsLabelledFormField("Email", groupClassName = "mt-2") {
                bsFormInput(it, UserEditForm::email, type = InputType.Email)
            }

            bsLabelledFormField("Phone", groupClassName = "mt-2") {
                bsFormInput(it, UserEditForm::phone, type = InputType.Tel)
            }

            div(className = "form-check mt-2") {
                fieldWithLabel("Admin", className = "form-check-label") {
                    checkBox(id = it, className = "form-check-input") {
                        bind(UserEditForm::isAdmin)
                    }
                }
            }
        }
    }
}

@Serializable
private data class UserEditForm(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false
) {
    fun toUserDto(id: Int, isActive: Boolean) = UserDto(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        isAdmin = isAdmin,
        isActive = isActive
    )
}

private fun UserDto.toUserEditForm() = UserEditForm(
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    isAdmin = isAdmin
)
