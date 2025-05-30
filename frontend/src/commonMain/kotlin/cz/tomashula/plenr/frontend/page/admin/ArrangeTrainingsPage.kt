package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import cz.tomashula.plenr.feature.training.CreateOrUpdateTrainingDto
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.frontend.Colors
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.*
import cz.tomashula.plenr.util.LocalTimeRange
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.contains
import cz.tomashula.plenr.util.now
import cz.tomashula.plenr.util.rangeTo
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.ui.Alignment
import dev.kilua.core.IComponent
import dev.kilua.form.Form
import dev.kilua.form.InputType
import dev.kilua.form.select.select
import dev.kilua.form.text.textArea
import dev.kilua.form.time.richDateTime
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.html.helpers.TagStyleFun.Companion.border
import dev.kilua.html.helpers.onClickLaunch
import dev.kilua.panel.hPanel
import dev.kilua.utils.cast
import dev.kilua.utils.toJsAny
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlinx.serialization.Serializable
import web.canvas.CanvasTextAlign
import web.canvas.CanvasTextBaseline
import web.uievents.MouseEvent
import kotlin.time.Duration.Companion.minutes

private data class TrainingView(
    val edited: Boolean,
    val created: Boolean,
    val training: TrainingWithParticipantsDto
)

private fun TrainingWithParticipantsDto.toTrainingView(
    edited: Boolean = false,
    created: Boolean = false
) = TrainingView(edited, created, this)

private fun newTraining(
    dateTime: LocalDateTime,
    arranger: UserDto
) = TrainingWithParticipantsDto(
    id = -1,
    arranger = arranger,
    name = "",
    description = "",
    type = TrainingType.DRESSAGE,
    startDateTime = dateTime,
    lengthMinutes = 60,
    participants = emptySet()
)

@Composable
fun IComponent.arrangeTrainingsPage(mainViewModel: MainViewModel)
{
    var users by remember { mutableStateOf(emptyList<UserDto>()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var trainings = remember { mutableStateMapOf<LocalDate, List<TrainingView>>() }
    var currentDialogTraining by remember { mutableStateOf<TrainingWithParticipantsDto?>(null) }
    /* Whether the current dialog is editing a training or creating a new one */
    var currentDialogTrainingEdit by remember { mutableStateOf(false) }
    val userAvailabilities = remember { mutableStateMapOf<LocalDate, Map<UserDto, LocalTimeRanges>>() }
    var tooltipX by remember { mutableStateOf(0) }
    var tooltipY by remember { mutableStateOf(0) }
    var tooltipUser by remember { mutableStateOf<UserDto?>(null) }

    val newOrModifiedTrainings by remember {
        derivedStateOf {
            trainings.values.flatten().filter { it.edited || it.created }
        }
    }

    LaunchedEffect(selectedDay) {
        users = mainViewModel.getAllUsers().filterNot { it.isAdmin }
            launch {
                userAvailabilities[selectedDay] = mainViewModel.getUsersAvailabilityForDay(users.map(
                    UserDto::id), selectedDay).mapKeys { users.find { userDto -> userDto.id == it.key }!! }
            }
    }

    LaunchedEffect(selectedDay) {
        if (trainings[selectedDay] == null)
            trainings[selectedDay] = mainViewModel.getAllTrainingsAdmin(
                from = selectedDay.atTime(0, 0),
                to = selectedDay.atTime(23, 59)
            ).map(TrainingWithParticipantsDto::toTrainingView)
    }

    trainingDialog(
        shown = currentDialogTraining != null,
        edit = currentDialogTrainingEdit,
        training = currentDialogTraining ?: newTraining(selectedDay.atTime(12, 0), mainViewModel.user!!),
        users = users,
        userAvailabilities = userAvailabilities,
        onSave = { saveTraining ->
            val originalTraining = currentDialogTraining!!
            val originalDate = originalTraining.startDateTime.date
            val saveDate = saveTraining.startDateTime.date
            if (currentDialogTrainingEdit)
                trainings[originalDate] = trainings[originalDate]!!.filterNot { it.training.id == originalTraining.id }
            trainings[saveDate] = trainings[saveDate]!! + saveTraining.toTrainingView(
                created = !currentDialogTrainingEdit,
                edited = currentDialogTrainingEdit
            )

            println(saveTraining)
            currentDialogTraining = null
        },
        onDismiss = { currentDialogTraining = null },
        onRemove = {
            // Only allow removing trainings that haven't been saved to backend yet
            if (currentDialogTraining != null && currentDialogTraining!!.id == -1) {
                val date = currentDialogTraining!!.startDateTime.date
                // Remove the training from the trainings map
                trainings[date] = trainings[date]!!.filterNot { it.training.id == -1 && it.created }
                // Close the dialog
                currentDialogTraining = null
            }
        }
    )

    tooltipUser?.let { userTooltip(it, tooltipX, tooltipY) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val todayDay = remember { LocalDate.now() }

        arrowSelector(
            selectedItem = selectedDay,
            onNext = { selectedDay = selectedDay.plus(1, DateTimeUnit.DAY) },
            onPrevious = { selectedDay = selectedDay.minus(1, DateTimeUnit.DAY) },
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

        val userAvailabilityHeightPx = 10
        val userAvailabilitySpacing = 1.px

        div {
            position(Position.Relative)
            alignSelf(AlignItems.Stretch)
            marginTop(20.px)
            div {
                position(Position.Absolute)
                top(0.px)
                left((-80).px)
                for (user in userAvailabilities[selectedDay]?.keys ?: emptySet())
                {
                    div {
                        color(Colors.getColor(user.id))
                        height(userAvailabilityHeightPx.px)
                        marginTop(userAvailabilitySpacing)
                        fontSize(0.5.rem)
                        +user.fullName
                    }
                }
            }
            div {
                position(Position.Relative)
                overflowX(Overflow.Auto)
                width(100.perc)
                height(100.perc)

                val timetableHeight = 500
                val timetableWidth = 2000

                val fromHour = 8
                val toHour = 21

                val spacing = timetableWidth / (toHour - fromHour)

                timetableBackground(
                    height = timetableHeight,
                    width = timetableWidth,
                    fromHour = fromHour,
                    toHour = toHour,
                    color = Color("#c6c6c6dd"),
                    onClick = { clickedTime ->
                        currentDialogTraining = newTraining(
                            dateTime = selectedDay.atTime(clickedTime.hour, 0),
                            arranger = mainViewModel.user!!
                        )
                        currentDialogTrainingEdit = false
                    }
                )

                /* Overlay */
                div {
                    position(Position.Absolute)
                    top(0.px)
                    height(timetableHeight.px)
                    width(timetableWidth.px)
                    style("pointer-events", "none")

                    for (user in userAvailabilities[selectedDay]?.keys ?: emptySet())
                        userAvailability(
                            user = user,
                            heightPx = userAvailabilityHeightPx,
                            marginTop = userAvailabilitySpacing,
                            hourWidth = spacing,
                            startHour = fromHour,
                            onMouseMove = { event ->
                                tooltipX = event.clientX
                                tooltipY = event.clientY
                                tooltipUser = user
                            },
                            onMouseExit = { tooltipUser = null },
                            availableTimeRanges = userAvailabilities[selectedDay]?.get(user)?.getRanges() ?: emptyList()
                        )

                    for (training in trainings[selectedDay] ?: emptyList())
                        trainingView(
                            trainingView = training,
                            hourWidth = spacing,
                            startHour = fromHour,
                            onEdit = { currentDialogTraining = it; currentDialogTrainingEdit = true }
                        )
                }
            }
        }


        if (newOrModifiedTrainings.isNotEmpty())
            bsButton(label = "Save") {
                onClickLaunch {
                    val createOrUpdateDtos = newOrModifiedTrainings.map { it.training.toCreateTrainingDto() }.toSet()
                    mainViewModel.arrangeTrainings(createOrUpdateDtos)
                    for (day in trainings.keys)
                        trainings[day] = trainings[day]!!.map { it.copy(edited = false, created = false) }
                }
            }
    }
}

@Composable
private fun IComponent.userTooltip(
    user: UserDto,
    x: Int,
    y: Int
)
{
    participantBadge(user) {
        position(Position.Absolute)
        style("pointer-events", "none")
        val offsetX = 10
        val offsetY = 10
        left((x + offsetX).px)
        top((y + offsetY).px)
        zIndex(10)
        border(width = 1.px, style = BorderStyle.Solid, Color.hex(0x000000))
    }
}

@Composable
private fun IComponent.userAvailability(
    user: UserDto,
    heightPx: Int,
    hourWidth: Int,
    startHour: Int,
    marginTop: CssSize,
    onMouseMove: (MouseEvent) -> Unit = {},
    onMouseExit: (MouseEvent) -> Unit = {},
    availableTimeRanges: List<LocalTimeRange>
)
{
    div {
        width(100.perc)
        marginTop(marginTop)
        height(heightPx.px)
        position(Position.Relative)
        overflowX(Overflow.Hidden)
        style("pointer-events", "auto")
        onEvent<MouseEvent>("mousemove") { onMouseMove(it) }
        onEvent<MouseEvent>("mouseleave") { onMouseExit(it) }

        for (range in availableTimeRanges)
            userAvailabilityPart(
                range = range,
                hourWidth = hourWidth,
                startHour = startHour,
                borderRadius = (heightPx / 2).px,
                color = Colors.getColor(user.id)
            )
    }
}

@Composable
private fun IComponent.userAvailabilityPart(
    range: LocalTimeRange,
    hourWidth: Int,
    startHour: Int,
    borderRadius: CssSize,
    color: Color
)
{
    val startMinute = range.start.hour * 60 + range.start.minute
    val endMinute = range.endInclusive.hour * 60 + range.endInclusive.minute
    val durationMinutes = endMinute - startMinute


    div {
        height(100.perc)
        top(0.px)
        background(color)
        borderRadius(borderRadius)
        position(Position.Absolute)
        left(((startMinute / 60f - startHour) * hourWidth).toInt().px)
        width((durationMinutes / 60f * hourWidth).toInt().px)
    }
}

@Composable
private fun IComponent.trainingDialog(
    shown: Boolean,
    edit: Boolean,
    training: TrainingWithParticipantsDto,
    users: List<UserDto>,
    userAvailabilities: Map<LocalDate, Map<UserDto, LocalTimeRanges>> = emptyMap(),
    onSave: (TrainingWithParticipantsDto) -> Unit,
    onDismiss: () -> Unit,
    onRemove: (() -> Unit)? = null
)
{
    var form: Form<TrainingForm>? = null
    var participants by remember(training) { mutableStateOf(training.participants) }
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

    @Composable
    fun IComponent.trainingParticipant(
        user: UserDto
    )
    {
        participantBadge(user) {
            cursor(Cursor.Pointer)
            style("user-select", "none")
            if (participants.contains(user))
                border(2.px, BorderStyle.Solid, Color.Black)
            else
                border(2.px, BorderStyle.Solid, Color("transparent"))
            onClick {
                if (participants.contains(user))
                    participants -= user
                else
                    participants += user
            }
        }
    }


    bsModalDialog(
        shown = shown,
        title = if (edit) "Edit Training" else "Create Training",
        onDismiss = onDismiss,
        footer = {
            bsButton("Cancel", style = ButtonStyle.BtnSecondary) {
                onClick { onDismiss() }
            }
            if (edit && !training.cancelled && training.id != -1) {
                bsButton("Cancel Training", style = ButtonStyle.BtnDanger) {
                    onClick {
                        form?.let {
                            onSave(
                                it.getData()
                                    .copy(cancelled = true)
                                    .toTrainingWithParticipantsDto(training.arranger)
                                    .copy(id = training.id)
                            )
                        }
                    }
                }
            }
            // Add Remove button for trainings that haven't been saved to backend yet
            if (training.id == -1 && onRemove != null) {
                bsButton("Remove", style = ButtonStyle.BtnDanger) {
                    onClick {
                        onRemove()
                    }
                }
            }
            bsButton("Save", style = ButtonStyle.BtnPrimary) {
                onClick {
                    form?.let {
                        onSave(
                            it.getData()
                                .toTrainingWithParticipantsDto(training.arranger)
                                .copy(id = training.id)
                        )
                    }
                }
            }
        }
    ) {
        form = bsFormRef<TrainingForm>(
            onSubmit = { data, _, _ ->
                onSave(data.toTrainingWithParticipantsDto(training.arranger).copy(id = training.id))
            }
        ) {
            LaunchedEffect(training) {
                setData(training.toTrainingForm())
            }

            LaunchedEffect(participants) {
                setData(getData().copy(participants = participants.toSet()))
            }

            bsLabelledFormField("Name") {
                bsFormInput(it, TrainingForm::name)
            }
            bsLabelledFormField("Description") {
                textArea(it, className = "form-control") {
                    bind(TrainingForm::description)
                }
            }
            bsLabelledFormField("Type") {
                select(id = it, className = "form-select") {
                    option(label = TrainingType.DRESSAGE.name.lowercase(), value = TrainingType.DRESSAGE.name)
                    option(label = TrainingType.PARKOUR.name.lowercase(), value = TrainingType.PARKOUR.name)
                    bindCustom(TrainingForm::type)
                    val dressageLength = 45
                    val parkourLength = 60
                    LaunchedEffect(training) {
                        val length = if (value?.lowercase() == TrainingType.PARKOUR.name.lowercase()) parkourLength else dressageLength
                        setData(getData().copy(lengthMinutes = length))
                    }
                    onChange {
                        val length = if (value?.lowercase() == TrainingType.PARKOUR.name.lowercase()) parkourLength else dressageLength
                        setData(getData().copy(lengthMinutes = length))
                    }
                }
            }
            bsLabelledFormField("Start") {
                richDateTime(id = it, format = "dd.MM.yyyy HH:mm") {
                    bind(TrainingForm::startDateTime)
                }
            }
            bsLabelledFormField("Length in minutes") {
                bsFormInput(it, type = InputType.Number) {
                    bindCustom(TrainingForm::lengthMinutes)
                }
            }
            bsLabelledFormField("Participants") {
                br()
                hPanel(
                    gap = 3.px,
                    rowGap = 3.px,
                    flexWrap = FlexWrap.Wrap,
                ) {
                    for (user in availableUsersSorted)
                        trainingParticipant(user)
                }

                var unavailableUsersExpanded by remember { mutableStateOf(participants.any { it in unavailableUsers }) }

                if (unavailableUsers.isNotEmpty())
                {
                    hPanel(
                        alignItems = AlignItems.Center,
                    ) {
                        cursor(Cursor.Pointer)
                        fontSize(0.8.rem)
                        color(Color.Gray)
                        spant("Unavailable users")
                        materialIconOutlined(if (unavailableUsersExpanded) "arrow_drop_up" else "arrow_drop_down")
                        onClick {
                            unavailableUsersExpanded = !unavailableUsersExpanded
                        }
                    }
                    if (unavailableUsersExpanded)
                        hPanel(
                            gap = 3.px,
                            rowGap = 3.px,
                            flexWrap = FlexWrap.Wrap,
                        ) {
                            for (user in unavailableUsers)
                                trainingParticipant(user)
                        }
                }
            }
        }
    }
}

private fun LocalTime.plusMinutes(minutes: Int): LocalTime
{
    val date = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val instant = this.atDate(date).toInstant(TimeZone.currentSystemDefault())
    val instantOffset = instant.plus(minutes.minutes)
    val offsetTime = instantOffset.toLocalDateTime(TimeZone.currentSystemDefault()).time
    return offsetTime
}

@Composable
private fun IComponent.trainingView(
    trainingView: TrainingView,
    hourWidth: Int,
    startHour: Int,
    onEdit: (TrainingWithParticipantsDto) -> Unit = {},
)
{
    val training = trainingView.training
    val startMinute = training.startDateTime.hour * 60 + training.startDateTime.minute
    val durationMinutes = training.lengthMinutes

    training(
        training = training,
        onClick = { onEdit(training) }
    ) {
        position(Position.Absolute)
        left(((startMinute / 60f - startHour) * hourWidth).toInt().px)
        width((durationMinutes / 60f * hourWidth).toInt().px)
        marginTop(30.px)

        if (trainingView.edited || trainingView.created)
            border(Border(2.px, BorderStyle.Dashed, Color.Black))
    }
}

@Composable
fun IComponent.timetableBackground(
    height: Int,
    width: Int,
    color: Color,
    fromHour: Int = 0,
    toHour: Int = 24,
    onClick: (LocalTime) -> Unit = {}
)
{
    canvas(
        canvasWidth = width,
        canvasHeight = height,
    ) {
        val ctx = context2D!!

        // TODO: This may be recalled during every recomposition. Make sure it is only called once.
        val canvasWidth = this.canvasWidth!!
        val canvasHeight = this.canvasHeight!!

        ctx.fillStyle = color.value.toJsAny()!!
        ctx.strokeStyle = color.value.toJsAny()!!
        ctx.textAlign = "center".cast<CanvasTextAlign>()
        ctx.textBaseline = "middle".cast<CanvasTextBaseline>()
        ctx.font = "20px " + ctx.font.split(" ").last()

        val totalHours = toHour - fromHour
        val spacing = canvasWidth / totalHours

        for (i in 0..<totalHours)
        {
            val x = i * spacing
            val hour = fromHour + i
            ctx.beginPath()
            ctx.moveTo(x + 0.5, 0.0)
            ctx.lineTo(x.toDouble(), canvasHeight.toDouble() - 35)
            ctx.stroke()
            ctx.fillText(hour.toString().padStart(2, '0'), x.toDouble(), canvasHeight.toDouble() - 20, maxWidth = 20.0)
        }

        onClick { clickEvent ->
            val relativeMinute = ((clickEvent.offsetX / spacing + fromHour) * 60).toInt()
            val clickedTime = LocalTime(relativeMinute / 60, relativeMinute % 60)
            onClick(clickedTime)
        }
    }
}

private val dateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
}

@Serializable
private data class TrainingForm(
    val name: String = "",
    val description: String = "",
    val startDateTime: LocalDateTime? = null,
    val type: TrainingType = TrainingType.DRESSAGE,
    val lengthMinutes: Int = 60,
    val participants: Set<UserDto> = emptySet(),
    val cancelled: Boolean = false
)
{
    fun toTrainingWithParticipantsDto(
        arranger: UserDto
    ) = TrainingWithParticipantsDto(
        id = -1,
        arranger = arranger,
        name = name,
        description = description,
        type = type,
        startDateTime = startDateTime ?: LocalDateTime.now(),
        lengthMinutes = lengthMinutes,
        participants = participants.toSet(),
        cancelled = cancelled
    )
}

private fun TrainingWithParticipantsDto.toTrainingForm() = TrainingForm(
    name = name,
    description = description,
    startDateTime = startDateTime,
    type = type,
    lengthMinutes = lengthMinutes,
    participants = participants.toSet(),
    cancelled = cancelled
)

private fun TrainingWithParticipantsDto.toCreateTrainingDto() = CreateOrUpdateTrainingDto(
    id = if (id == -1) null else id,
    name = name,
    description = description,
    type = type,
    startDateTime = startDateTime,
    lengthMinutes = lengthMinutes,
    participantIds = participants.map { it.id }.toSet(),
    cancelled = cancelled
)
