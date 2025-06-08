package cz.tomashula.plenr.frontend.screen.manageusers

import cz.tomashula.plenr.feature.user.UserDto

data class ManageUsersScreenState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = true,
    val selectedUser: UserDto? = null,
    val isEditDialogShown: Boolean = false,
    val isDeleteDialogShown: Boolean = false,
    val isAddDialogShown: Boolean = false
)
