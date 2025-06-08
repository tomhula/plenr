package cz.tomashula.plenr.frontend.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {
    var uiState by mutableStateOf(LoginScreenState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }

    fun login(onSuccess: () -> Unit) {
        if (uiState.email.isBlank() || uiState.password.isBlank()) {
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val loginResult = appViewModel.login(uiState.email, uiState.password)
                if (loginResult) {
                    onSuccess()
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Invalid email or password",
                        password = ""
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred",
                    password = ""
                )
            }
        }
    }
}
