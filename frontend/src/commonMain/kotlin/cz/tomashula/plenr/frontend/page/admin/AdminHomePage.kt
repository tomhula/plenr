package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.*
import cz.tomashula.plenr.util.Week
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Alignment
import dev.kilua.compose.ui.Modifier
import dev.kilua.compose.ui.fillMaxWidth
import dev.kilua.core.IComponent
import dev.kilua.html.bsButton
import dev.kilua.html.px
import kotlinx.datetime.atTime

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
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

            trainingTypeLegend()
        }
        trainingCalendar(
            selectedWeek = selectedWeek,
            onWeekChange = { selectedWeek = it },
            trainings = arrangedTrainings,
            onTrainingClick = { trainingDialogState.show(it) }
        )
    }
}
