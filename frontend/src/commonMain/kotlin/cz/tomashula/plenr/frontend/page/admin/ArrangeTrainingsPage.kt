package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import dev.kilua.core.IComponent
import dev.kilua.html.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.component.bsFormInput
import cz.tomashula.plenr.frontend.component.bsFormRef
import cz.tomashula.plenr.frontend.component.bsLabelledFormField
import cz.tomashula.plenr.frontend.component.bsModalDialog
import cz.tomashula.plenr.frontend.component.materialIconOutlined
import cz.tomashula.plenr.util.now
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.ui.Alignment
import dev.kilua.form.Form
import dev.kilua.form.InputType
import dev.kilua.form.select.select
import dev.kilua.form.time.richDateTime
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import dev.kilua.utils.cast
import dev.kilua.utils.toJsAny
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import web.dom.CanvasTextAlign
import web.dom.CanvasTextBaseline

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
    id =  -1,
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

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers()
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
            trainings[originalDate] = trainings[originalDate]!!.filterNot { it.training.id == originalTraining.id }
            trainings[saveDate] = trainings[saveDate]!! + saveTraining.toTrainingView(created = !currentDialogTrainingEdit, edited = currentDialogTrainingEdit)

            println(saveTraining)
            currentDialogTraining = null
        },
        onDismiss = { currentDialogTraining = null }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        daySelector(
            day = selectedDay,
            onDayChange = { selectedDay = it }
        )

        div {
            marginTop(20.px)
            position(Position.Relative)
            overflowX(Overflow.Auto)
            alignSelf(AlignItems.Stretch)

            val timetableHeight = 300
            val timetableWidth = 3000

            timetableBackground(timetableHeight, timetableWidth, Color("#c6c6c6dd"))

            /* Overlay */
            div {
                position(Position.Absolute)
                top(0.px)
                height(timetableHeight.px)
                width(timetableWidth.px)

                for (training in trainings[selectedDay] ?: emptyList())
                    training(
                        trainingView = training,
                        onEdit = { currentDialogTraining = it; currentDialogTrainingEdit = true }
                    )
            }
        }
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
                    form?.let { onSave(it.getData().toTrainingWithParticipantsDto(training.arranger)) }
                }
            }
        }
    ) {
        form = bsFormRef<TrainingForm>(
            onSubmit = { data, _, _ ->
                onSave(data.toTrainingWithParticipantsDto(training.arranger))
            }
        ) {
            setData(training.toTrainingForm())

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
private fun IDiv.training(
    trainingView: TrainingView,
    onEdit: (TrainingWithParticipantsDto) -> Unit = {},
)
{
    val training = trainingView.training
    val totalMinutes = 24 * 60f
    val startMinute = training.startDateTime.hour * 60 + training.startDateTime.minute
    val durationMinutes = training.lengthMinutes

    vPanel {
        position(Position.Absolute)
        left((startMinute / totalMinutes * 100).perc)
        width((durationMinutes / totalMinutes * 100).perc)
        padding(5.px)
        fontSize(0.8.rem)
        borderRadius(5.px)
        cursor(Cursor.Pointer)
        onClick { onEdit(training) }
        background(Color.Bisque)
        if (trainingView.edited || trainingView.created)
            border(Border(2.px, BorderStyle.Dashed, Color.Black))

        val timeZone = TimeZone.currentSystemDefault()
        val startTimeStr = training.startDateTime.format(localDateTimeFormat)
        val endTimeStr = training.startDateTime.toInstant(timeZone).plus(durationMinutes, DateTimeUnit.MINUTE).toLocalDateTime(
            timeZone
        ).format(localDateTimeFormat)

        title("$startTimeStr - $endTimeStr")

        spant(training.name) {
            fontWeight(FontWeight.Bold)
        }
        spant(training.type.name.lowercase())
    }
}

@Composable
fun IComponent.timetableBackground(
    height: Int,
    width: Int,
    color: Color
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
    }
}

@Composable
private fun IComponent.daySelector(
    day: LocalDate,
    onDayChange: (LocalDate) -> Unit
)
{
    val dayDifference = day.minus(LocalDate.now()).days
    val text = when (dayDifference)
    {
        -1 -> "Yesterday"
        0 -> "Today"
        1 -> "Tomorrow"
        2 -> "Day After Tomorrow"
        else -> day.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + " " + day.format(dateFormat)
    }

    hPanel(
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.SpaceBetween
    ) {
        style("user-select", "none")
        fontSize(1.5.rem)
        width(370.px)
        materialIconOutlined("chevron_left") {
            fontSize(3.rem)
            cursor(Cursor.Pointer)
            onClick {
                onDayChange(day.plus(-1, DateTimeUnit.DAY))
            }
        }
        bt(text)
        materialIconOutlined("chevron_right") {
            fontSize(3.rem)
            cursor(Cursor.Pointer)
            onClick {
                onDayChange(day.plus(1, DateTimeUnit.DAY))
            }
        }
    }
}

private val dateFormat = LocalDate.Format {
    monthNumber()
    char('.')
    dayOfMonth()
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
