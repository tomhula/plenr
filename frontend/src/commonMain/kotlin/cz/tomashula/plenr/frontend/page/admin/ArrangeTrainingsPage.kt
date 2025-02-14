package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.form.select.select
import dev.kilua.html.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.applyColumn
import cz.tomashula.plenr.frontend.component.formField
import cz.tomashula.plenr.frontend.component.onSubmit
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.component.materialIconOutlined
import cz.tomashula.plenr.util.now
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Alignment
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import dev.kilua.utils.cast
import dev.kilua.utils.toJsAny
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import web.dom.CanvasTextAlign
import web.dom.CanvasTextBaseline

@Composable
fun IComponent.arrangeTrainingsPage(mainViewModel: MainViewModel)
{
    val router = Router.current

    var users by remember { mutableStateOf(emptyList<UserDto>()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var trainings = remember { mutableStateMapOf<LocalDate, List<TrainingWithParticipantsDto>>() }

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers()
    }

    LaunchedEffect(selectedDay) {
        if (trainings[selectedDay] == null)
            trainings[selectedDay] = mainViewModel.getAllTrainingsAdmin(
                from = selectedDay.atTime(0, 0),
                to = selectedDay.atTime(23, 59)
            ).also { println(it) }
    }

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
                    training(training)
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
    training: TrainingWithParticipantsDto
)
{
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
        background(Color.Bisque)

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
private fun IComponent.trainingCreationForm(
    state: TrainingCreationFormState,
    users: List<UserDto>,
    onSubmit: (TrainingCreationFormState) -> Unit,
    onChange: (TrainingCreationFormState) -> Unit
)
{
    form(id = "arrange-training-form", className = "form") {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        h1t("Arrange Trainings")

        onSubmit { onSubmit(state) }

        formField(
            inputId = "title",
            label = "Title",
            value = state.title,
            required = true,
            onChange = { onChange(state.copy(title = it)) }
        )

        formField(
            inputId = "description",
            label = "Description",
            value = state.description,
            type = InputType.Text,
            onChange = { onChange(state.copy(description = it)) }
        )

        formField(
            inputId = "date",
            label = "Date",
            value = state.startDateTime?.format(LocalDateTime.Formats.ISO) ?: "",
            type = InputType.DatetimeLocal,
            required = true,
            onChange = {
                onChange(state.copy(startDateTime = it.let {
                    try
                    {
                        LocalDateTime.parse(it, LocalDateTime.Formats.ISO)
                    }
                    catch (e: Exception)
                    {
                        null
                    }
                }))
            }
        )

        div("form-field") {
            applyColumn()
            label(className = "form-field-label") {
                htmlFor("type")
                +"Type"
            }
            select(className = "form-field-input", required = true) {
                id("type")
                option {
                    value("dressage")
                    +"Dressage"
                }
                option {
                    value("parkour")
                    +"Parkour"
                }
                onChange { onChange(state.copy(type = this.value ?: "dressage")) }
            }
        }

        formField(
            inputId = "length",
            label = "Length (minutes)",
            value = state.lengthMinutes.toString(),
            type = InputType.Number,
            required = true,
            onChange = { onChange(state.copy(lengthMinutes = it.toIntOrNull() ?: 0)) }
        )

        div(className = "form-field") {
            applyColumn()
            label(htmlFor = "users", className = "form-field-label") {
                +"Users"
            }
            val clients = remember(users) { users.filter { !it.isAdmin } }
            if (clients.isEmpty())
                +"No users available"
            else
                div(id = "users") {
                    applyColumn()
                    rowGap(10.px)
                    clients.forEach { user ->
                        val selected = state.participants.contains(user)
                        div(className = "user-card ${if (selected) "selected" else ""}") {
                            +"${user.firstName} ${user.lastName}"
                            onClick {
                                if (!selected)
                                    onChange(state.copy(participants = state.participants + user))
                                else
                                    onChange(state.copy(participants = state.participants - user))
                            }
                        }
                    }
                }
        }

        button("Create Training", type = ButtonType.Submit, className = "primary-button")
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

private data class TrainingCreationFormState(
    val title: String = "",
    val description: String = "",
    val startDateTime: LocalDateTime? = null,
    val type: String = "dressage",
    val lengthMinutes: Int = 0,
    val participants: Set<UserDto> = emptySet()
)
