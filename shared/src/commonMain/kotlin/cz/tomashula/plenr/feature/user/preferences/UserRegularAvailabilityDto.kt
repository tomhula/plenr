package cz.tomashula.plenr.feature.user.preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserRegularAvailabilityDto(
    val userId: Int,
    val availableTimes: WeeklyTimeRanges
)