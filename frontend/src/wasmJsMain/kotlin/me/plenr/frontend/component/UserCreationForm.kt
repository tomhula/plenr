package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.*
import me.tomasan7.plenr.feature.user.UserDto

@Composable
fun IComponent.userCreationForm(
    state: UserCreationFormState,
    onChange: (UserCreationFormState) -> Unit
)
{
    div {
        className("user-creation-form")
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        div {
            applyColumn()
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

        div {
            applyColumn()
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

        div {
            applyColumn()
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

        div {
            applyColumn()
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
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phoneNumber,
            isActive = false,
            isAdmin = isAdmin
        )
    }
}