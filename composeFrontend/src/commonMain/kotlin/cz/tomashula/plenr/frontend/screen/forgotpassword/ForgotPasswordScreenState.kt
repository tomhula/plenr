package cz.tomashula.plenr.frontend.screen.forgotpassword

data class ForgotPasswordScreenState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
