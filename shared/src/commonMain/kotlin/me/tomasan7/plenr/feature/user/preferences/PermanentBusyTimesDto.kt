package me.tomasan7.plenr.feature.user.preferences

import kotlinx.serialization.Serializable

@Serializable
data class PermanentBusyTimesDto(
    val userId: Int,
    val busyTimes: WeeklyTimeRanges
)