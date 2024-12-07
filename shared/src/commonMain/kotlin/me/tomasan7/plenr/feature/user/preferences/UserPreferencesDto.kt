package me.tomasan7.plenr.feature.user.preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesDto(
    val trainingsPerWeek: Int,
    val trainingArrangedNotiEmail: Boolean,
    val trainingArrangedNotiSms: Boolean,
    val trainingMovedNotiEmail: Boolean,
    val trainingMovedNotiSms: Boolean,
    val trainingCancelledNotiEmail: Boolean,
    val trainingCancelledNotiSms: Boolean
)