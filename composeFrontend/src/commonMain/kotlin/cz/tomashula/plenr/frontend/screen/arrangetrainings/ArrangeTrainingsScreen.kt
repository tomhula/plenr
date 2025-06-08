package cz.tomashula.plenr.frontend.screen.arrangetrainings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.ui.component.ArrowSelector
import cz.tomashula.plenr.frontend.ui.component.ParticipantBadge
import cz.tomashula.plenr.frontend.ui.component.Training
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.now
import cz.tomashula.plenr.util.contains
import kotlinx.datetime.*
import kotlinx.datetime.format.char

@Composable
fun ArrangeTrainingsScreen(
    viewModel: ArrangeTrainingsScreenViewModel
) {
    val uiState = viewModel.uiState
    val selectedDay = uiState.selectedDay ?: return
    val todayDay = remember { LocalDate.now() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ArrowSelector(
            selectedItem = selectedDay,
            onNext = { viewModel.onDayChange(selectedDay.plus(1, DateTimeUnit.DAY)) },
            onPrevious = { viewModel.onDayChange(selectedDay.minus(1, DateTimeUnit.DAY)) },
            itemDisplay = {
                when (selectedDay.minus(todayDay).days) {
                    -1 -> "Yesterday"
                    0 -> "Today"
                    1 -> "Tomorrow"
                    2 -> "Day After Tomorrow"
                    else -> selectedDay.dayOfWeek.name.lowercase()
                        .replaceFirstChar { it.uppercase() } + " " + selectedDay.format(dateFormat)
                }
            }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            // Display trainings for the selected day
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Trainings for ${selectedDay.format(dateFormat)}")

                uiState.trainings[selectedDay]?.forEach { trainingView ->
                    Training(
                        training = trainingView.training,
                        onClick = { viewModel.onTrainingClick(trainingView.training) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { 
                        viewModel.onNewTrainingClick(selectedDay.atTime(12, 0)) 
                    }
                ) {
                    Text("Add Training")
                }
            }

            // Save button for modified trainings
            val newOrModifiedTrainings = uiState.trainings.values.flatten().filter { it.edited || it.created }
            if (newOrModifiedTrainings.isNotEmpty()) {
                Button(
                    onClick = { viewModel.saveTrainings() }
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    // Training dialog
    if (uiState.currentDialogTraining != null) {
        TrainingDialog(
            training = uiState.currentDialogTraining,
            isEdit = uiState.isCurrentDialogTrainingEdit,
            users = uiState.users,
            userAvailabilities = uiState.userAvailabilities,
            onSave = { viewModel.onDialogSave(it) },
            onDismiss = { viewModel.onDialogDismiss() },
            onRemove = { viewModel.onDialogRemove() }
        )
    }
}

@Composable
fun TrainingDialog(
    training: TrainingWithParticipantsDto,
    isEdit: Boolean,
    users: List<UserDto>,
    userAvailabilities: Map<LocalDate, Map<UserDto, LocalTimeRanges>>,
    onSave: (TrainingWithParticipantsDto) -> Unit,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    var name by remember { mutableStateOf(training.name) }
    var description by remember { mutableStateOf(training.description) }
    var type by remember { mutableStateOf(training.type) }
    var startDateTime by remember { mutableStateOf(training.startDateTime) }
    var lengthMinutes by remember { mutableStateOf(training.lengthMinutes.toString()) }
    var participants by remember { mutableStateOf(training.participants) }

    val availableUsersSorted = remember(training, users, userAvailabilities) {
        users.sortedBy { it.fullName }.toSet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Training" else "Create Training") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selection
                Text("Type")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = type == TrainingType.DRESSAGE,
                        onClick = { type = TrainingType.DRESSAGE }
                    )
                    Text("Dressage")

                    Spacer(Modifier.width(8.dp))

                    RadioButton(
                        selected = type == TrainingType.PARKOUR,
                        onClick = { type = TrainingType.PARKOUR }
                    )
                    Text("Parkour")
                }

                // Length in minutes
                OutlinedTextField(
                    value = lengthMinutes,
                    onValueChange = { lengthMinutes = it },
                    label = { Text("Length (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Participants
                Text("Participants")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableUsersSorted.toList()) { user ->
                        Box(
                            modifier = Modifier.clickable {
                                participants = if (participants.contains(user)) {
                                    participants - user
                                } else {
                                    participants + user
                                }
                            }
                        ) {
                            ParticipantBadge(
                                participant = user,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        training.copy(
                            name = name,
                            description = description,
                            type = type,
                            startDateTime = startDateTime,
                            lengthMinutes = lengthMinutes.toIntOrNull() ?: 60,
                            participants = participants
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (training.id == -1) {
                    TextButton(
                        onClick = onRemove
                    ) {
                        Text("Remove")
                    }
                }
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

private val dateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
}
