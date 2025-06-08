package cz.tomashula.plenr.frontend.screen.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

class ForgotPasswordScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {
    var uiState by mutableStateOf(ForgotPasswordScreenState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun requestPasswordReset() {
        if (uiState.email.isBlank()) {
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, isSuccess = false)

        viewModelScope.launch {
            try {
                appViewModel.requestPasswordReset(uiState.email)
                uiState = uiState.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }
}
