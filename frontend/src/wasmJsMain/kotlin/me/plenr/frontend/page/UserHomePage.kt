package me.plenr.frontend.page

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.html.*
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.tomasan7.plenr.feature.training.TrainingWithParticipantsDto

@Composable
fun IComponent.userHomePage(viewModel: MainViewModel)
{
    var arrangedTrainings: List<TrainingWithParticipantsDto>? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        arrangedTrainings = viewModel.getMyTrainings()
    }

    div {
        applyColumn()
        rowGap(10.px)

        // TODO: This will be a calendar view
        h2t("My trainings:", className = "user-trainings-header")

        if (arrangedTrainings == null)
        {
            pt("Loading trainings...")
            return@div
        }
        else if (arrangedTrainings!!.isEmpty())
        {
            pt("You have no trainings.")
            return@div
        }

        arrangedTrainings!!.forEach { training ->
            trainingCard(training)
        }
    }
}

@Composable
fun IComponent.trainingCard(training: TrainingWithParticipantsDto)
{
    div(className = "training-card") {
        pt(className = "training-card-name", text = training.name)
        div(className = "training-card-details") {
            p {
                strongt("Description: ")
                +training.description
            }
            p {
                strongt("Type: ")
                +training.type.toString().lowercase().replaceFirstChar { it.uppercase() }
            }
            p {
                strongt("Start Date and Time: ")
                +training.startDateTime.toString() // TODO: Readable format
            }
            p {
                strongt("Length: ")
                +training.lengthMinutes.toString()
                +" minutes"
            }
            p {
                strongt("Participants: ")
                +training.participants.joinToString { "${it.firstName} ${it.lastName}" }
            }
        }
    }
}