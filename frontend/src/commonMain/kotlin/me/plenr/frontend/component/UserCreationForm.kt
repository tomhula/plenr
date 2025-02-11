package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import me.tomasan7.plenr.feature.user.UserDto

@Composable
fun IComponent.userCreationFormFields(
    state: UserCreationFormState,
    onChange: (UserCreationFormState) -> Unit
)
{
    formField(
        inputId = "first-name-field",
        label = "First Name",
        value = state.firstName,
        type = InputType.Text,
        onChange = { onChange(state.copy(firstName = it)) }
    )

    formField(
        inputId = "last-name-field",
        label = "Last Name",
        value = state.lastName,
        type = InputType.Text,
        onChange = { onChange(state.copy(lastName = it)) }
    )

    formField(
        inputId = "email-field",
        label = "Email",
        value = state.email,
        type = InputType.Email,
        onChange = { onChange(state.copy(email = it)) }
    )

    formField(
        inputId = "phone-number-field",
        label = "Phone Number",
        value = state.phoneNumber,
        type = InputType.Tel,
        onChange = { onChange(state.copy(phoneNumber = it)) }
    )
}

data class UserCreationFormState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)
{
    fun toUserDto(isAdmin: Boolean = false): UserDto
    {
        return UserDto(
            id = -1,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phoneNumber,
            isActive = false,
            isAdmin = isAdmin
        )
    }
}