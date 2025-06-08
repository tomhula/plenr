package cz.tomashula.plenr.frontend.screen.setpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

class SetPasswordScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {
    var uiState by mutableStateOf(SetPasswordScreenState())
        private set

    fun setToken(token: String) {
        uiState = uiState.copy(token = token)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword)
    }

    fun setPassword(onSuccess: () -> Unit) {
        if (uiState.token.isBlank() || uiState.password.isBlank() || uiState.confirmPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "All fields are required")
            return
        }

        if (uiState.password != uiState.confirmPassword) {
            uiState = uiState.copy(errorMessage = "Passwords do not match")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                appViewModel.setPassword(uiState.token, uiState.password)
                uiState = uiState.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }
}
