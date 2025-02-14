package cz.tomashula.plenr.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun LocalDate.Companion.now(): LocalDate = LocalDateTime.now().date

fun LocalDateTime.Companion.now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
