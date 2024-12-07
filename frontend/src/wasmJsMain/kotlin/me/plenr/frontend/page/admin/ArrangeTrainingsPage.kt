package me.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import app.softwork.routingcompose.navigate
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.form.select.select
import dev.kilua.html.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.formField
import me.plenr.frontend.component.onSubmit
import me.tomasan7.plenr.feature.training.TrainingType
import me.tomasan7.plenr.feature.user.UserDto

@Composable
fun IComponent.arrangeTrainingsPage(mainViewModel: MainViewModel)
{
    val router = Router.current
    val coroutineScope = rememberCoroutineScope()

    var state by remember { mutableStateOf(TrainingCreationFormState()) }
    var users by remember { mutableStateOf(emptyList<UserDto>()) }

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers()
    }

    trainingCreationForm(
        state,
        onSubmit = { submitState ->
            coroutineScope.launch {
                mainViewModel.createTraining(
                    submitState.title,
                    submitState.description,
                    submitState.startDateTime!!,
                    TrainingType.valueOf(submitState.type.uppercase()),
                    submitState.lengthMinutes,
                    submitState.participants.map { it.id }
                )
            }
            window.alert("Training created")
            state = TrainingCreationFormState()
        },
        onChange = { state = it },
        users = users
    )
}

@Composable
private fun IComponent.trainingCreationForm(
    state: TrainingCreationFormState,
    users: List<UserDto>,
    onSubmit: (TrainingCreationFormState) -> Unit,
    onChange: (TrainingCreationFormState) -> Unit
)
{
    form(id = "arrange-training-form") {
        className("training-creation-form")
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

private data class TrainingCreationFormState(
    val title: String = "",
    val description: String = "",
    val startDateTime: LocalDateTime? = null,
    val type: String = "dressage",
    val lengthMinutes: Int = 0,
    val participants: Set<UserDto> = emptySet()
)