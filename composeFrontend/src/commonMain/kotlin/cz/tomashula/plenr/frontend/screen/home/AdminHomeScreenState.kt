package cz.tomashula.plenr.frontend.screen.home

import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.util.Week

data class AdminHomeScreenState(
    val selectedWeek: Week = Week.current(),
    val trainings: Set<TrainingWithParticipantsDto> = emptySet(),
)
