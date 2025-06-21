package cz.tomashula.plenr.frontend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class PlenrScreen
{
    val title: String
        get() = this::class.simpleName!! // TODO: Replace with StringResource for each destination
    
    @Serializable
    @SerialName("admin-setup")
    data object AdminSetup : PlenrScreen()
    @Serializable
    @SerialName("set-password")
    data class SetPassword(val token: String) : PlenrScreen()
    @Serializable
    @SerialName("login")
    data object Login : PlenrScreen()
    @Serializable
    @SerialName("home")
    data object Home : PlenrScreen()
    @Serializable
    @SerialName("availability")
    data object Availability : PlenrScreen()
    @Serializable
    @SerialName("preferences")
    data object Preferences : PlenrScreen()
    @Serializable
    @SerialName("arrange-trainings")
    data object ArrangeTrainings : PlenrScreen()
    @Serializable
    @SerialName("manage-users")
    data object ManageUsers : PlenrScreen()
    @Serializable
    @SerialName("forgot-password")
    data object ForgotPassword : PlenrScreen()
}
