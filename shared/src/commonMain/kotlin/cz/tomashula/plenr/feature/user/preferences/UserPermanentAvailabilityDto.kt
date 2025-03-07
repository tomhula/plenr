package cz.tomashula.plenr.feature.user.preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserPermanentAvailabilityDto(
    val userId: Int,
    val availableTimes: WeeklyTimeRanges
)
