package me.tomasan7.plenr.adduser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.screen.Screen
import me.tomasan7.plenr.frontend.generated.resources.*
import org.jetbrains.compose.resources.stringResource

class AddUserScreen : Screen
{
    @Composable
    override fun Content()
    {
        val model = remember { AddUserScreenModel() }
        val uiState = model.uiState

        Column {
            FirstNameField(uiState.name) { model.setName(it) }
            LastNameField(uiState.surname) { model.setSurname(it) }
            PhoneField(uiState.phone) { model.setPhone(it) }
            EmailField(uiState.email) { model.setEmail(it) }

            Button(onClick = model::onAddUserClick) {
                Text(stringResource(Res.string.add_user_button))
            }
        }
    }

    @Composable
    private fun FirstNameField(name: String, onValueChange: (String) -> Unit) {
        TextField(
            label = { Text(stringResource(Res.string.first_name)) },
            value = name,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }

    @Composable
    private fun LastNameField(surname: String, onValueChange: (String) -> Unit) {
        TextField(
            label = { Text(stringResource(Res.string.last_name)) },
            value = surname,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }

    @Composable
    private fun PhoneField(phone: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
        TextField(
            label = { Text(stringResource(Res.string.phone)) },
            value = phone,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false
            )
        )
    }

    @Composable
    private fun EmailField(email: String, onValueChange: (String) -> Unit)
    {
        TextField(
            label = { Text(stringResource(Res.string.email)) },
            value = email,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
    }
}