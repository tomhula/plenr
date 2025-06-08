package cz.tomashula.plenr.frontend.screen.arrangetrainings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.ui.component.ArrowSelector
import cz.tomashula.plenr.frontend.ui.component.ParticipantBadge
import cz.tomashula.plenr.frontend.ui.component.Training
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.now
import kotlinx.datetime.*
import kotlinx.datetime.format.char

@Composable
fun ArrangeTrainingsScreen(
    viewModel: ArrangeTrainingsScreenViewModel
)
{
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
                when (selectedDay.minus(todayDay).days)
                {
                    -1 -> "Yesterday"
                    0 -> "Today"
                    1 -> "Tomorrow"
                    2 -> "Day After Tomorrow"
                    else -> selectedDay.dayOfWeek.name.lowercase()
                        .replaceFirstChar { it.uppercase() } + " " + selectedDay.format(dateFormat)
                }
            }
        )

        if (uiState.isLoading)
            CircularProgressIndicator()
        else
        {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                val fromHour = 8
                val toHour = 21
                var timetableSize by remember { mutableStateOf(IntSize.Zero) }
                val spacing = timetableSize.width / (toHour - fromHour)
                
                TimetableBackground(
                    fromHour = fromHour,
                    toHour = toHour,
                    color = Color.Gray,
                    onClick = { clickedTime ->
                        viewModel.onNewTrainingClick(selectedDay.atTime(clickedTime.hour, 0))
                    },
                    modifier = Modifier.matchParentSize().onSizeChanged { size ->
                        timetableSize = size
                    }
                )
                for (trainingView in uiState.trainings[selectedDay] ?: emptyList())
                    TrainingView(
                        trainingView = trainingView,
                        hourWidth = spacing,
                        startHour = fromHour,
                        onEdit = { viewModel.onTrainingClick(it) }
                    )
            }

            // Save button for modified trainings
            val newOrModifiedTrainings = uiState.trainings.values.flatten().filter { it.edited || it.created }
            if (newOrModifiedTrainings.isNotEmpty())
            {
                Button(
                    onClick = { viewModel.saveTrainings() }
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    // Training dialog
    if (uiState.currentDialogTraining != null)
    {
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
private fun TimetableBackground(
    fromHour: Int,
    toHour: Int,
    color: Color,
    onClick: (LocalTime) -> Unit = {},
    modifier: Modifier = Modifier
)
{
    val textMeasurer = rememberTextMeasurer()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged {
                canvasSize = it
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val totalHours = toHour - fromHour
                    val spacing = canvasSize.width / totalHours
                    val relativeMinute = ((offset.x / spacing + fromHour) * 60).toInt()
                    val clickedTime = LocalTime(relativeMinute / 60, relativeMinute % 60)
                    onClick(clickedTime)
                }
            }
    ) {
        val totalHours = toHour - fromHour
        val spacing = size.width / totalHours

        for (i in 0 until totalHours)
        {
            val x = i * spacing + .5f
            val hour = fromHour + i
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height - 30)
            )
            val hourText = textMeasurer.measure(hour.toString().padStart(2, '0'))
            drawText(
                textLayoutResult = hourText,
                topLeft = Offset(x - hourText.size.width / 2, size.height - 20),
            )
        }
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
)
{
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableUsersSorted.forEach { user ->
                        Box(
                            modifier = Modifier.clickable {
                                participants = if (participants.contains(user))
                                    participants - user
                                else
                                    participants + user
                            }
                        ) {
                            val isSelected = user in participants
                            val borderModifier = if (isSelected) Modifier.border(width = 2.dp, color = Color.Black) else Modifier
                            ParticipantBadge(
                                participant = user,
                                modifier = Modifier.padding(4.dp)
                                    .then(borderModifier)
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
                if (training.id == -1)
                {
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

@Composable
fun TrainingView(
    trainingView: TrainingView,
    hourWidth: Int,
    startHour: Int,
    onEdit: (TrainingWithParticipantsDto) -> Unit = {},
)
{
    val training = trainingView.training
    val startMinute = training.startDateTime.hour * 60 + training.startDateTime.minute
    val durationMinutes = training.lengthMinutes

    Training(
        training = training,
        onClick = { onEdit(training) },
        modifier = Modifier
            .width((durationMinutes / 60f * hourWidth).dp)
            .offset(x = ((startMinute / 60f - startHour) * hourWidth).dp)
    )
}

private val dateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
}
