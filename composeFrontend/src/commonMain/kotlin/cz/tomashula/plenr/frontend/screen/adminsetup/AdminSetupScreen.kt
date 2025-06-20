package cz.tomashula.plenr.frontend.screen.adminsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.tomashula.plenr.frontend.AppViewModel

@Composable
fun AdminSetupScreen(
    appViewModel: AppViewModel,
    onAdminSetupFinished: () -> Unit = {},
    viewModel: AdminSetupScreenViewModel = viewModel { AdminSetupScreenViewModel(appViewModel) }
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(300.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Admin Setup")

        if (uiState.isSuccess) {
            Text("Admin account created. Check your email to set your password.")

            Button(
                onClick = onAdminSetupFinished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Login")
            }
        } else {
            TextField(
                value = uiState.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = { Text("Phone Number") },
                singleLine = true,
                placeholder = { Text("+000 000 000 000") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red
                )
            }

            Button(
                onClick = { viewModel.createAdminAccount() },
                enabled = !uiState.isLoading && 
                          uiState.firstName.isNotBlank() && 
                          uiState.lastName.isNotBlank() && 
                          uiState.email.isNotBlank() && 
                          uiState.phoneNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Create Admin Account")
            }
        }
    }
}
