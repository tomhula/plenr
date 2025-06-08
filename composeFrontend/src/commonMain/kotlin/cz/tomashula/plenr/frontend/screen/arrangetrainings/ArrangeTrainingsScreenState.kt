package cz.tomashula.plenr.frontend.screen.arrangetrainings

import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.Week
import kotlinx.datetime.LocalDate

data class ArrangeTrainingsScreenState(
    val selectedDay: LocalDate? = null,
    val trainings: Map<LocalDate, List<TrainingView>> = emptyMap(),
    val users: List<UserDto> = emptyList(),
    val userAvailabilities: Map<LocalDate, Map<UserDto, LocalTimeRanges>> = emptyMap(),
    val currentDialogTraining: TrainingWithParticipantsDto? = null,
    val isCurrentDialogTrainingEdit: Boolean = false,
    val isLoading: Boolean = true
)

data class TrainingView(
    val edited: Boolean,
    val created: Boolean,
    val training: TrainingWithParticipantsDto
)

fun TrainingWithParticipantsDto.toTrainingView(
    edited: Boolean = false,
    created: Boolean = false
) = TrainingView(edited, created, this)
