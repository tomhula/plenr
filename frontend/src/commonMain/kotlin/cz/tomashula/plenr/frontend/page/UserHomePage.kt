package cz.tomashula.plenr.frontend.page

import androidx.compose.runtime.*
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.bsObjectDialog
import cz.tomashula.plenr.frontend.component.rememberObjectDialogState
import cz.tomashula.plenr.frontend.component.trainingCalendar
import cz.tomashula.plenr.frontend.component.trainingDialogBody
import cz.tomashula.plenr.util.Week
import dev.kilua.core.IComponent
import kotlinx.datetime.atTime

@Composable
fun IComponent.userHomePage(viewModel: MainViewModel)
{
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
        viewer = viewModel.user,
        onWeekChange = { selectedWeek = it },
        trainings = arrangedTrainings,
        onTrainingClick = { trainingDialogState.show(it) }
    )
}
