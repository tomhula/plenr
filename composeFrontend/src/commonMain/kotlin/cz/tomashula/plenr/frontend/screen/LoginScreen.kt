package cz.tomashula.plenr.frontend.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    appViewModel: AppViewModel,
    onLogin: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
)
{
    Column {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text("Email")
        TextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
        )

        Text("Password")
        TextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
        )

        TextButton(
            onClick = onForgotPassword
        ) {
            Text("Forgot password?")
        }

        Button(
            onClick = {
                appViewModel.viewModelScope.launch {
                    val loginResult = appViewModel.login(email, password)
                    if (loginResult)
                        onLogin()
                    else
                    {
                        email = ""
                        password = ""
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Login")
        }
    }
}
