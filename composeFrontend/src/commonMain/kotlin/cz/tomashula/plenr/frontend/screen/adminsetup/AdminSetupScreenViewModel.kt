package cz.tomashula.plenr.frontend.screen.adminsetup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

class AdminSetupScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {
    var uiState by mutableStateOf(AdminSetupScreenState())
        private set

    fun onFirstNameChange(firstName: String) {
        uiState = uiState.copy(firstName = firstName)
    }

    fun onLastNameChange(lastName: String) {
        uiState = uiState.copy(lastName = lastName)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        uiState = uiState.copy(phoneNumber = phoneNumber)
    }

    fun createAdminAccount() {
        if (uiState.firstName.isBlank() || uiState.lastName.isBlank() || 
            uiState.email.isBlank() || uiState.phoneNumber.isBlank()) {
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, isSuccess = false)

        viewModelScope.launch {
            try {
                val userDto = UserDto(
                    id = -1,
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    email = uiState.email,
                    phone = uiState.phoneNumber,
                    isActive = false,
                    isAdmin = true
                )
                appViewModel.createUser(userDto)
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
