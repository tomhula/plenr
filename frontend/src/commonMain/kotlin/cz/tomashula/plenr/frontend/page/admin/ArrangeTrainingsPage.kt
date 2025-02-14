package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
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
import dev.kilua.externals.tempusDominusLocales
import dev.kilua.form.time.richDateTime
import dev.kilua.utils.cast
import dev.kilua.utils.toJsAny
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import web.dom.CanvasTextAlign
import web.dom.CanvasTextBaseline

@Composable
fun IComponent.arrangeTrainingsPage(mainViewModel: MainViewModel)
{
    val router = Router.current

    var users by remember { mutableStateOf(emptyList<UserDto>()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        h1t("Arrange trainings", className = "mb-5") {
            textAlign(TextAlign.Center)
        }

        daySelector(
            day = selectedDay,
            onDayChange = { selectedDay = it }
        )

        val topOffset = 20

        div {
            position(Position.Relative)

            timetableBackground(600, 1800, topOffset, Color("#c6c6c6dd"))

            div {
                position(Position.Absolute)
                top(topOffset.px)
                height(100.perc)
                width(100.perc)
            }
        }
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
            onChange = { onChange(state.copy(startDateTime = it.let {
                try {
                    LocalDateTime.parse(it, LocalDateTime.Formats.ISO)
                }
                catch (e: Exception) {
                    null
                }
            })) }
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
    topOffset: Int,
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
        ctx.textAlign = "right".cast<CanvasTextAlign>()
        ctx.textBaseline = "middle".cast<CanvasTextBaseline>()

        val totalLines = 24
        val spacing = canvasHeight / totalLines

        for (i in 0..23)
        {
            val y = topOffset + i * spacing
            ctx.beginPath()
            ctx.moveTo(0.0, y + 0.5)
            ctx.lineTo(canvasWidth.toDouble() - 20, y.toDouble())
            ctx.stroke()
            ctx.fillText(i.toString().padStart(2, '0'), canvasWidth.toDouble() - 5, y.toDouble(), maxWidth = 20.0)
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.px)
    ) {
        materialIconOutlined("chevron_left") {
            onClick {
                onDayChange(day.plus(-1, DateTimeUnit.DAY))
            }
        }
        bt(text)
        materialIconOutlined("chevron_right") {
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
