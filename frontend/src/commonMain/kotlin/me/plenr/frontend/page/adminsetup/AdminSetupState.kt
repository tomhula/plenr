package me.plenr.frontend.page.adminsetup

import me.plenr.frontend.component.UserCreationFormState

data class AdminSetupState(
    val userCreationFormState: UserCreationFormState = UserCreationFormState()
)