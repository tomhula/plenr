package cz.tomashula.plenr.frontend.page

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.bsObjectDialog
import cz.tomashula.plenr.frontend.component.participantBadge
import cz.tomashula.plenr.frontend.component.rememberObjectDialogState
import cz.tomashula.plenr.frontend.component.trainingCalendar
import cz.tomashula.plenr.util.Week
import dev.kilua.core.IComponent
import dev.kilua.html.bt
import dev.kilua.html.div
import dev.kilua.html.px
import dev.kilua.html.spant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

@Composable
fun IComponent.userHomePage(viewModel: MainViewModel)
{
    val router = Router.current
    var selectedWeek by remember { mutableStateOf(Week.current()) }
    var arrangedTrainings by remember { mutableStateOf(emptySet<TrainingWithParticipantsDto>()) }
    var oldestFetchedWeek: Week? by remember { mutableStateOf(null) }
    val trainingDialogState = rememberObjectDialogState<TrainingWithParticipantsDto>()

    LaunchedEffect(selectedWeek) {
        if (oldestFetchedWeek == null || selectedWeek < oldestFetchedWeek!!)
        {
            arrangedTrainings += viewModel.getMyTrainings(selectedWeek.mondayDate.atTime(0, 0), oldestFetchedWeek?.mondayDate?.atTime(0, 0))
            oldestFetchedWeek = selectedWeek
        }
    }

    bsObjectDialog(trainingDialogState, "Training details", { trainingDialogState.hide() }) {
        trainingDialogBody(it, viewModel.user!!)
    }

    trainingCalendar(
        selectedWeek = selectedWeek,
        onWeekChange = { selectedWeek = it },
        trainings = arrangedTrainings,
        onTrainingClick = { trainingDialogState.show(it) }
    )
}


@Composable
private fun IComponent.trainingDialogBody(
    training: TrainingWithParticipantsDto,
    currentUser: UserDto
)
{
    div {
        marginTop(10.px)
        bt("Name: ")
        spant(training.name)
    }
    div {
        marginTop(10.px)
        bt("Description: ")
        spant(training.description)
    }
    div {
        marginTop(10.px)
        bt("Start: ")
        spant(training.startDateTime.format(dateTimeFormat))
    }
    div {
        marginTop(10.px)
        bt("Length: ")
        spant("${training.lengthMinutes}min")
    }
    div {
        marginTop(10.px)
        bt("Participants: ")
        for (participant in training.participants.filterNot { it.id == currentUser.id })
            participantBadge(participant) {
                marginLeft(5.px)
            }
    }
}

private val dateTimeFormat = LocalDateTime.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    chars(". ")
    hour()
    char(':')
    minute()
}
