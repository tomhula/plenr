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
import cz.tomashula.plenr.util.now
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.ui.Alignment
import dev.kilua.core.IComponent
import dev.kilua.form.Form
import dev.kilua.form.InputType
import dev.kilua.form.select.select
import dev.kilua.form.time.richDateTime
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.html.helpers.TagStyleFun.Companion.border
import dev.kilua.html.helpers.onClickLaunch
import dev.kilua.utils.cast
import dev.kilua.utils.toJsAny
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.serialization.Serializable
import web.dom.CanvasTextAlign
import web.dom.CanvasTextBaseline
import web.dom.events.MouseEvent

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
    val permanentBusyTimes = remember { mutableStateMapOf<UserDto, WeeklyTimeRanges>() }
    val permanentAvailableTimes by derivedStateOf {
        permanentBusyTimes.mapValues { it.value.inverted() }
    }
    var tooltipX by remember { mutableStateOf(0) }
    var tooltipY by remember { mutableStateOf(0) }
    var tooltipUser by remember { mutableStateOf<UserDto?>(null) }

    val newOrModifiedTrainings by derivedStateOf {
        trainings.values.flatten().filter { it.edited || it.created }
    }

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers().filterNot { it.isAdmin }
        for (user in users)
            launch {
                permanentBusyTimes[user] = mainViewModel.getPermanentBusyTimesAdmin(user.id)
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
        onDismiss = { currentDialogTraining = null }
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
                for (user in permanentAvailableTimes.keys)
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
                val timetableWidth = 3000

                timetableBackground(
                    height = timetableHeight,
                    width = timetableWidth,
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

                    for ((user, availableTimeRanges) in permanentAvailableTimes)
                        userAvailability(
                            user = user,
                            heightPx = userAvailabilityHeightPx,
                            marginTop = userAvailabilitySpacing,
                            onMouseMove = { event ->
                                tooltipX = event.clientX
                                tooltipY = event.clientY
                                tooltipUser = user
                            },
                            onMouseExit = { tooltipUser = null },
                            availableTimeRanges = availableTimeRanges.getRangesForDay(selectedDay.dayOfWeek)
                        )

                    for (training in trainings[selectedDay] ?: emptyList())
                        trainingView(
                            trainingView = training,
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
        val offsetX = -(element.clientWidth / 2)
        val offsetY = -element.clientHeight - 5
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
        style("pointer-events", "auto")
        onEvent<MouseEvent>("mousemove") { onMouseMove(it) }
        onEvent<MouseEvent>("mouseleave") { onMouseExit(it) }

        for (range in availableTimeRanges)
            userAvailabilityPart(
                range = range,
                borderRadius = (heightPx / 2).px,
                color = Colors.getColor(user.id)
            )
    }
}

@Composable
private fun IComponent.userAvailabilityPart(
    range: LocalTimeRange,
    borderRadius: CssSize,
    color: Color
)
{
    val totalMinutes = 24 * 60f
    val startMinute = range.start.hour * 60 + range.start.minute
    val endMinute = range.endInclusive.hour * 60 + range.endInclusive.minute
    val durationMinutes = endMinute - startMinute


    div {
        height(100.perc)
        top(0.px)
        background(color)
        borderRadius(borderRadius)
        position(Position.Absolute)
        left((startMinute / totalMinutes * 100).perc)
        width((durationMinutes / totalMinutes * 100).perc)
    }
}

@Composable
private fun IComponent.trainingDialog(
    shown: Boolean,
    edit: Boolean,
    training: TrainingWithParticipantsDto,
    users: List<UserDto>,
    onSave: (TrainingWithParticipantsDto) -> Unit,
    onDismiss: () -> Unit
)
{
    var form: Form<TrainingForm>? = null
    var participants by remember(training) { mutableStateOf(training.participants) }
    val participantsAlphabetically by derivedStateOf {
        participants.sortedBy { it.fullName }
    }
    val usersAlphabetically by derivedStateOf {
        users.sortedBy { it.fullName }
    }

    bsModalDialog(
        shown = shown,
        title = if (edit) "Edit Training" else "Create Training",
        onDismiss = onDismiss,
        footer = {
            bsButton("Cancel", style = ButtonStyle.BtnSecondary) {
                onClick { onDismiss() }
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
                bsFormInput(it, TrainingForm::description)
            }
            bsLabelledFormField("Type") {
                select(id = it, className = "form-select") {
                    option(label = TrainingType.DRESSAGE.name.lowercase(), value = TrainingType.DRESSAGE.name)
                    option(label = TrainingType.PARKOUR.name.lowercase(), value = TrainingType.PARKOUR.name)
                    bindCustom(TrainingForm::type)
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
                bsFormInput(id = it, required = false) {
                    list("usersList")
                    onInput { event ->
                        val value = this@bsFormInput.value ?: return@onInput
                        val user = users.find { it.fullName == value.trim() }
                        if (user != null)
                        {
                            this@bsFormInput.value = ""
                            participants = participants + user
                        }
                    }
                }
                datalist(id = "usersList") {
                    for (user in usersAlphabetically)
                        option(label = user.fullName, value = user.fullName)
                }
                for (participant in participantsAlphabetically)
                    divt(participant.fullName) {
                        onClick {
                            participants = participants - participant
                        }
                    }
            }
        }
    }
}

private val localDateTimeFormat = LocalDateTime.Format {
    hour(Padding.ZERO)
    char(':')
    minute(Padding.ZERO)
}

@Composable
private fun IComponent.trainingView(
    trainingView: TrainingView,
    onEdit: (TrainingWithParticipantsDto) -> Unit = {},
)
{
    val training = trainingView.training
    val totalMinutes = 24 * 60f
    val startMinute = training.startDateTime.hour * 60 + training.startDateTime.minute
    val durationMinutes = training.lengthMinutes

    training(
        training = training,
        onClick = { onEdit(training) }
    ) {
        position(Position.Absolute)
        left((startMinute / totalMinutes * 100).perc)
        width((durationMinutes / totalMinutes * 100).perc)
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

        ctx.fillStyle = color.value.toJsAny()
        ctx.strokeStyle = color.value.toJsAny()
        ctx.textAlign = "center".cast<CanvasTextAlign>()
        ctx.textBaseline = "middle".cast<CanvasTextBaseline>()
        ctx.font = "20px " + ctx.font.split(" ").last()

        val totalLines = 24
        val spacing = canvasWidth / totalLines

        for (i in 0..23)
        {
            val x = i * spacing
            ctx.beginPath()
            ctx.moveTo(x + 0.5, 0.0)
            ctx.lineTo(x.toDouble(), canvasHeight.toDouble() - 35)
            ctx.stroke()
            ctx.fillText(i.toString().padStart(2, '0'), x.toDouble(), canvasHeight.toDouble() - 20, maxWidth = 20.0)
        }

        onClick { clickEvent ->
            val relativeMinute = (clickEvent.offsetX / spacing * 60).toInt()
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
    val participants: Set<UserDto> = emptySet()
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
        participants = participants.toSet()
    )
}

private fun TrainingWithParticipantsDto.toTrainingForm() = TrainingForm(
    name = name,
    description = description,
    startDateTime = startDateTime,
    type = type,
    lengthMinutes = lengthMinutes,
    participants = participants.toSet()
)

private fun TrainingWithParticipantsDto.toCreateTrainingDto() = CreateOrUpdateTrainingDto(
    id = if (id == -1) null else id,
    name = name,
    description = description,
    type = type,
    startDateTime = startDateTime,
    lengthMinutes = lengthMinutes,
    participantIds = participants.map { it.id }.toSet()
)
