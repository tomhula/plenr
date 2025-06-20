package cz.tomashula.plenr.frontend

import kotlinx.serialization.Serializable

sealed class PlenrScreen
{
    val title: String
        get() = this::class.simpleName!! // TODO: Replace with StringResource for each destination
    
    @Serializable
    data object AdminSetup : PlenrScreen()
    @Serializable
    data class SetPassword(val token: String) : PlenrScreen()
    @Serializable
    data object Login : PlenrScreen()
    @Serializable
    data object Home : PlenrScreen()
    @Serializable
    data object Availability : PlenrScreen()
    @Serializable
    data object Preferences : PlenrScreen()
    @Serializable
    data object ArrangeTrainings : PlenrScreen()
    @Serializable
    data object ManageUsers : PlenrScreen()
    @Serializable
    data object ForgotPassword : PlenrScreen()
}
