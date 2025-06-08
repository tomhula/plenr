package cz.tomashula.plenr.frontend.screen.arrangetrainings

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontVariation.width
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.ui.Colors
import cz.tomashula.plenr.frontend.ui.component.ArrowSelector
import cz.tomashula.plenr.frontend.ui.component.ParticipantBadge
import cz.tomashula.plenr.frontend.ui.component.Training
import cz.tomashula.plenr.util.LocalTimeRange
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.contains
import cz.tomashula.plenr.util.now
import cz.tomashula.plenr.util.rangeTo
import io.ktor.http.HttpHeaders.Position
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.jetbrains.skia.Surface
import kotlin.time.Duration.Companion.minutes

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
            },
            modifier = Modifier.width(330.dp)
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

                Column {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (user in uiState.users)
                        {
                            val userAvailability = uiState.userAvailabilities[selectedDay]?.get(user)
                            if (userAvailability != null)
                            {
                                UserAvailability(
                                    user = user,
                                    startHour = fromHour,
                                    hourWidth = spacing,
                                    availableTimeRanges = userAvailability,
                                    modifier = Modifier.fillMaxWidth().height(12.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                    Box {
                        for (trainingView in uiState.trainings[selectedDay] ?: emptyList())
                            TrainingView(
                                trainingView = trainingView,
                                hourWidth = spacing,
                                startHour = fromHour,
                                onEdit = { viewModel.onTrainingClick(it) }
                            )
                    }
                }
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
        users
            .filter { user ->
                userAvailabilities[training.startDateTime.date]?.get(user)?.let { ranges ->
                    ranges.getRanges().any { range ->
                        // TODO: Maybe use LocalTimeRanges methods?
                        training.startDateTime.time..training.startDateTime.time.plusMinutes(training.lengthMinutes) in range
                    }
                } == true
            }
            .sortedBy { it.fullName }
            .toSet()
    }

    val unavailableUsers = remember(users, availableUsersSorted) {
        users.toSet() - availableUsersSorted
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
                            val borderModifier = if (isSelected)
                                Modifier.border(width = 2.dp, color = Color.Black)
                            else
                                Modifier
                            ParticipantBadge(
                                participant = user,
                                modifier = Modifier.padding(4.dp)
                                    .then(borderModifier)
                            )
                        }
                    }
                }

                var unavailableUsersShown by remember { mutableStateOf(false) }

                if (!unavailableUsers.isEmpty())
                {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            unavailableUsersShown = !unavailableUsersShown
                        }
                    ) {
                        Text("Unavailable Users")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    if (unavailableUsersShown)
                    {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            unavailableUsers.forEach { user ->
                                Box(
                                    modifier = Modifier.clickable {
                                        participants = if (participants.contains(user))
                                            participants - user
                                        else
                                            participants + user
                                    }
                                ) {
                                    val isSelected = user in participants
                                    val borderModifier = if (isSelected)
                                        Modifier.border(width = 2.dp, color = Color.Black)
                                    else
                                        Modifier
                                    ParticipantBadge(
                                        participant = user,
                                        modifier = Modifier.padding(4.dp)
                                            .then(borderModifier)
                                    )
                                }
                            }
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

private fun LocalTime.plusMinutes(minutes: Int): LocalTime
{
    val date = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val instant = this.atDate(date).toInstant(TimeZone.currentSystemDefault())
    val instantOffset = instant.plus(minutes.minutes)
    val offsetTime = instantOffset.toLocalDateTime(TimeZone.currentSystemDefault()).time
    return offsetTime
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserAvailability(
    user: UserDto,
    startHour: Int,
    hourWidth: Int,
    availableTimeRanges: LocalTimeRanges,
    modifier: Modifier = Modifier
)
{
    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                color = Colors.getColor(user.id),
            ) {
                Text(
                    text = user.fullName,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        state = rememberBasicTooltipState()
    ) {
        Box(
            modifier = modifier
        ) {
            for (timeRange in availableTimeRanges.getRanges())
            {
                UserAvailabilityPart(
                    range = timeRange,
                    hourWidth = hourWidth,
                    startHour = startHour,
                    color = Colors.getColor(user.id)
                )
            }
        }
    }
}

@Composable
private fun UserAvailabilityPart(
    range: LocalTimeRange,
    hourWidth: Int,
    startHour: Int,
    color: Color,
    modifier: Modifier = Modifier
)
{
    val startMinute = range.start.hour * 60 + range.start.minute
    val endMinute = range.endInclusive.hour * 60 + range.endInclusive.minute
    val durationMinutes = endMinute - startMinute

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width((durationMinutes / 60f * hourWidth).dp)
            .offset(x = ((startMinute / 60f - startHour) * hourWidth).dp)
            .background(color)
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
