package cz.tomashula.plenr.frontend.screen.manageusers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.AppViewModel
import kotlinx.coroutines.launch

class ManageUsersScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {
    var uiState by mutableStateOf(ManageUsersScreenState())
        private set

    init {
        refreshUserList()
    }

    private fun refreshUserList() {
        viewModelScope.launch {
            try {
                val allUsers = appViewModel.getAllUsers()
                val usersExceptMe = allUsers - appViewModel.user!!
                uiState = uiState.copy(
                    users = usersExceptMe,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onEditClick(user: UserDto) {
        uiState = uiState.copy(
            selectedUser = user,
            isEditDialogShown = true
        )
    }

    fun onDeleteClick(user: UserDto) {
        uiState = uiState.copy(
            selectedUser = user,
            isDeleteDialogShown = true
        )
    }

    fun onEditDialogDismiss() {
        uiState = uiState.copy(isEditDialogShown = false)
    }

    fun onDeleteDialogDismiss() {
        uiState = uiState.copy(isDeleteDialogShown = false)
    }

    fun onAddClick() {
        uiState = uiState.copy(isAddDialogShown = true)
    }

    fun onAddDialogDismiss() {
        uiState = uiState.copy(isAddDialogShown = false)
    }

    fun updateUser(user: UserDto) {
        viewModelScope.launch {
            try {
                val success = appViewModel.updateUser(user)
                if (success) {
                    refreshUserList()
                }
                uiState = uiState.copy(isEditDialogShown = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                val success = appViewModel.deleteUser(userId)
                if (success) {
                    refreshUserList()
                }
                uiState = uiState.copy(isDeleteDialogShown = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createUser(user: UserDto) {
        viewModelScope.launch {
            try {
                val userId = appViewModel.createUser(user)
                if (userId > 0) {
                    refreshUserList()
                }
                uiState = uiState.copy(isAddDialogShown = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
