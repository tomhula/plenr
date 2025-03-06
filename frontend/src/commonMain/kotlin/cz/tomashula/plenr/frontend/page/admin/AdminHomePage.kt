package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsObjectDialog
import cz.tomashula.plenr.frontend.component.participantBadge
import cz.tomashula.plenr.frontend.component.rememberObjectDialogState
import cz.tomashula.plenr.frontend.component.trainingCalendar
import cz.tomashula.plenr.util.Week
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.core.IComponent
import dev.kilua.html.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

@Composable
fun IComponent.adminHomePage(viewModel: MainViewModel)
{
    val router = Router.current
    var selectedWeek by remember { mutableStateOf(Week.current()) }
    var arrangedTrainings by remember { mutableStateOf(emptySet<TrainingWithParticipantsDto>()) }
    var oldestFetchedWeek: Week? by remember { mutableStateOf(null) }
    val trainingDialogState = rememberObjectDialogState<TrainingWithParticipantsDto>()

    LaunchedEffect(selectedWeek) {
        if (oldestFetchedWeek == null || selectedWeek < oldestFetchedWeek!!)
        {
            /* FIX: Currently the admin sees all trainings, even those not arranged by him.
            *   Make it so only his trainings show or it is somehow distinguished */
            arrangedTrainings += viewModel.getAllTrainingsAdmin(selectedWeek.mondayDate.atTime(0, 0), oldestFetchedWeek?.mondayDate?.atTime(0, 0))
            oldestFetchedWeek = selectedWeek
        }
    }

    bsObjectDialog(trainingDialogState, "Training details", { trainingDialogState.hide() }) {
        trainingDialogBody(it)
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.px),
        ) {
            bsButton("Manage users") {
                onClick {
                    router.navigate(Route.MANAGE_USERS)
                }
            }
            bsButton("Arrange trainings") {
                onClick {
                    router.navigate(Route.ARRANGE_TRAININGS)
                }
            }
        }
        trainingCalendar(
            selectedWeek = selectedWeek,
            onWeekChange = { selectedWeek = it },
            trainings = arrangedTrainings,
            onTrainingClick = { trainingDialogState.show(it) }
        )
    }
}

@Composable
private fun IComponent.trainingDialogBody(
    training: TrainingWithParticipantsDto
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
        for (participant in training.participants)
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
