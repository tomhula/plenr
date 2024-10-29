package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.div
import dev.kilua.html.label
import me.tomasan7.plenr.api.UserDto

@Composable
fun IComponent.userCreationForm(
    state: UserCreationFormState,
    onChange: (UserCreationFormState) -> Unit
)
{
    div(className = "user-creation-form") {
        label {
            htmlFor("first-name")
            +"First Name"
        }
        text(state.firstName) {
            className("first-name")
            id("first-name")
            type(InputType.Text)
            onInput { onChange(state.copy(firstName = this.value ?: "")) }
        }
        label {
            htmlFor("last-name")
            +"Last Name"
        }
        text(state.lastName) {
            className("last-name")
            id("last-name")
            type(InputType.Text)
            onInput { onChange(state.copy(lastName = this.value ?: "")) }
        }
        label {
            htmlFor("email")
            +"Email"
        }
        text(state.email) {
            className("email")
            id("email")
            type(InputType.Email)
            onInput { onChange(state.copy(email = this.value ?: "")) }
        }
        label {
            htmlFor("phone-number")
            +"Phone Number"
        }
        text(state.phoneNumber) {
            className("phone-number")
            id("phone-number")
            type(InputType.Tel)
            onInput { onChange(state.copy(phoneNumber = this.value ?: "")) }
        }
    }
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
            name = "$firstName $lastName",
            email = email,
            phone = phoneNumber,
            isActive = false,
            isAdmin = isAdmin
        )
    }
}