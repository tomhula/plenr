package me.tomasan7.plenr.adminsetup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel

class AdminSetupScreenModel : ScreenModel
{
    var uiState by mutableStateOf(AdminSetupScreenState())
        private set

    fun setFirstName(firstName: String)
    {
        uiState = uiState.copy(firstName = firstName.trim())
    }

    fun setLastName(lastName: String)
    {
        uiState = uiState.copy(lastName = lastName.trim())
    }

    fun setEmail(email: String)
    {
        uiState = uiState.copy(email = email.trim())
    }

    fun setPhoneNumber(phoneNumber: String)
    {
        uiState = uiState.copy(phoneNumber = phoneNumber.trim())
    }

    fun setPassword(password: String)
    {
        uiState = uiState.copy(password = password.trim())
    }

    fun setConfirmationPassword(confirmationPassword: String)
    {
        uiState = uiState.copy(confirmationPassword = confirmationPassword.trim())
    }
}