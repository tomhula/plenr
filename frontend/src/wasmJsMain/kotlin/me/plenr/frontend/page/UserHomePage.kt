package me.plenr.frontend.page

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.html.h2t
import dev.kilua.html.pt
import me.plenr.frontend.MainViewModel
import me.tomasan7.plenr.feature.training.TrainingWithParticipantsDto

@Composable
fun IComponent.UserHomePage(viewModel: MainViewModel)
{
    var arrangedTrainings: List<TrainingWithParticipantsDto>? by remember { mutableStateOf(listOf()) }

    LaunchedEffect(Unit) {
        arrangedTrainings = viewModel.getMyTrainings()
    }

    h2t("My trainings:")

    if (arrangedTrainings == null)
    {
        pt("Loading trainings...")
        return
    }
    else if (arrangedTrainings!!.isEmpty())
    {
        pt("You have no trainings.")
        return
    }

    arrangedTrainings!!.forEach { training ->
        pt("${training.name} on ${training.startDateTime}")
        pt(training.toString())
    }
}