package me.tomasan7.plenr.adminsetup

data class AdminSetupScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmationPassword: String = ""
)