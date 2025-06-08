package cz.tomashula.plenr.frontend.screen.setpassword

data class SetPasswordScreenState(
    val token: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
