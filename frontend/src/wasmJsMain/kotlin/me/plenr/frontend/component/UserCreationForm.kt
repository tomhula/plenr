package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.*
import me.tomasan7.plenr.api.UserDto

@Composable
fun IComponent.userCreationForm(
    state: UserCreationFormState,
    onChange: (UserCreationFormState) -> Unit
)
{
    column(
        alignItems = AlignItems.Center
    ) {
        className("user-creation-form")
        rowGap(10.px)

        column {
            label {
                htmlFor("first-name")
                +"First Name"
            }
            text(state.firstName) {
                id("first-name")
                type(InputType.Text)
                onInput { onChange(state.copy(firstName = this.value ?: "")) }
            }
        }

        column {
            label {
                htmlFor("last-name")
                +"Last Name"
            }
            text(state.lastName) {
                id("last-name")
                type(InputType.Text)
                onInput { onChange(state.copy(lastName = this.value ?: "")) }
            }
        }

        column {
            label {
                htmlFor("email")
                +"Email"
            }
            text(state.email) {
                id("email")
                type(InputType.Email)
                onInput { onChange(state.copy(email = this.value ?: "")) }
            }
        }

        column {
            label {
                htmlFor("phone-number")
                +"Phone Number"
            }
            text(state.phoneNumber) {
                id("phone-number")
                type(InputType.Tel)
                onInput { onChange(state.copy(phoneNumber = this.value ?: "")) }
            }
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