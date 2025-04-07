package cz.tomashula.plenr.feature.user.preferences

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserPreferencesService : RemoteService
{
    /**
     * Allows each user to get their own preferences.
     * Allows admins to get any user's preferences.
     */
    // CONSIDER: Don't allow admins to get any user's whole preferences.
    suspend fun getUserPreferences(userId: Int, authToken: String): UserPreferencesDto?

    /**
     * Allows each user to set their own preferences.
     */
    suspend fun setUserPreferences(userId: Int, userPreferencesDto: UserPreferencesDto, authToken: String)
}
