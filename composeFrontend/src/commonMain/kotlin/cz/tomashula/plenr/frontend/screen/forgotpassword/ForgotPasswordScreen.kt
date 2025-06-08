package cz.tomashula.plenr.frontend.screen.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.tomashula.plenr.frontend.AppViewModel

@Composable
fun ForgotPasswordScreen(
    appViewModel: AppViewModel,
    onBackToLogin: () -> Unit = {},
    viewModel: ForgotPasswordScreenViewModel = viewModel { ForgotPasswordScreenViewModel(appViewModel) }
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Forgot Password")

        if (uiState.isSuccess) {
            Text("Password reset email has been sent. Please check your inbox.")
            
            Button(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Login")
            }
        } else {
            TextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red
                )
            }

            Button(
                onClick = { viewModel.requestPasswordReset() },
                enabled = !uiState.isLoading && uiState.email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Reset Password")
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Back to Login")
            }
        }
    }
}
