package cz.tomashula.plenr.frontend.page.adminsetup

import cz.tomashula.plenr.frontend.component.UserCreationFormState

data class AdminSetupState(
    val userCreationFormState: UserCreationFormState = UserCreationFormState()
)
