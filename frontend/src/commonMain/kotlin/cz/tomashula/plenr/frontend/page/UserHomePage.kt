package cz.tomashula.plenr.frontend.page

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.trainingCalendar
import cz.tomashula.plenr.util.Week
import dev.kilua.core.IComponent
import kotlinx.datetime.atTime

@Composable
fun IComponent.userHomePage(viewModel: MainViewModel)
{
    val router = Router.current
    var selectedWeek by remember { mutableStateOf(Week.current()) }
    var arrangedTrainings by remember { mutableStateOf(emptySet<TrainingWithParticipantsDto>()) }
    var oldestFetchedWeek: Week? by remember { mutableStateOf(null) }

    LaunchedEffect(selectedWeek) {
        if (oldestFetchedWeek == null || selectedWeek < oldestFetchedWeek!!)
        {
            arrangedTrainings += viewModel.getMyTrainings(selectedWeek.mondayDate.atTime(0, 0), oldestFetchedWeek?.mondayDate?.atTime(0, 0))
            oldestFetchedWeek = selectedWeek
        }
    }

    trainingCalendar(
        selectedWeek = selectedWeek,
        onWeekChange = { selectedWeek = it },
        trainings = arrangedTrainings,
        onTrainingClick = { println("Training clicked: ${it.name}") }
    )
}
