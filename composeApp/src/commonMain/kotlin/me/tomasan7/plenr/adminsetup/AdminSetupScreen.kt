package me.tomasan7.plenr.adminsetup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import me.tomasan7.plenr.frontend.generated.resources.*
import org.jetbrains.compose.resources.stringResource

class AdminSetupScreen : Screen
{
    @Composable
    override fun Content()
    {
        val model = rememberScreenModel { AdminSetupScreenModel() }

        Column {
            Text(stringResource(Res.string.admin_setup_title))

            FirstNameField(model.uiState.firstName) { model.setFirstName(it) }
            LastNameField(model.uiState.lastName) { model.setLastName(it) }
            PhoneField(model.uiState.phoneNumber) { model.setPhoneNumber(it) }
            EmailField(model.uiState.email) { model.setEmail(it) }
            PasswordField(model.uiState.password) { model.setPassword(it) }
            PasswordConfirmationField(model.uiState.confirmationPassword) { model.setConfirmationPassword(it) }
        }
    }

    @Composable
    private fun FirstNameField(name: String, onValueChange: (String) -> Unit)
    {
        TextField(
            label = { Text(stringResource(Res.string.first_name)) },
            value = name,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }

    @Composable
    private fun LastNameField(surname: String, onValueChange: (String) -> Unit)
    {
        TextField(
            label = { Text(stringResource(Res.string.last_name)) },
            value = surname,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }

    @Composable
    private fun PhoneField(phone: String, onValueChange: (String) -> Unit)
    {
        TextField(
            label = { Text(stringResource(Res.string.phone)) },
            value = phone,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
                autoCorrect = false
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

    @Composable
    private fun PasswordField(password: String, onValueChange: (String) -> Unit)
    {
        var passwordVisible by remember { mutableStateOf(false) }

        TextField(
            label = { Text(stringResource(Res.string.password)) },
            value = password,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } }
        )
    }

    @Composable
    private fun PasswordConfirmationField(passwordConfirmation: String, onValueChange: (String) -> Unit)
    {
        var passwordVisible by remember { mutableStateOf(false) }

        TextField(
            label = { Text(stringResource(Res.string.confirm_password)) },
            value = passwordConfirmation,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } }
        )
    }

    @Composable
    private fun PasswordVisibilityToggle(passwordVisible: Boolean, onToggle: () -> Unit)
    {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = stringResource(if (passwordVisible) Res.string.hide_password else Res.string.show_password)
            )
        }
    }
}