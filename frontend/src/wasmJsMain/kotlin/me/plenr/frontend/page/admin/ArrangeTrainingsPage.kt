package me.plenr.frontend.page.admin

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.select.select
import dev.kilua.form.text.text
import dev.kilua.html.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.tomasan7.plenr.feature.training.TrainingType
import me.tomasan7.plenr.feature.user.UserDto

@Composable
fun IComponent.arrangeTrainingsPage(mainViewModel: MainViewModel)
{
    val coroutineScope = rememberCoroutineScope()

    var state by remember { mutableStateOf(TrainingCreationFormState()) }
    var users by remember { mutableStateOf(emptyList<UserDto>()) }

    LaunchedEffect(Unit) {
        users = mainViewModel.getAllUsers()
    }

    h2t("Arrange Trainings")
    trainingCreationForm(
        state,
        onSubmit = { state ->
            coroutineScope.launch {
                mainViewModel.createTraining(
                    state.title,
                    state.description,
                    state.startDateTime!!,
                    TrainingType.valueOf(state.type.uppercase()),
                    state.lengthMinutes,
                    state.participants.map { it.id }
                )
            }
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
    div {
        className("training-creation-form")
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        div {
            applyColumn()
            label {
                htmlFor("title")
                +"Title"
            }
            text(state.title) {
                id("title")
                type(InputType.Text)
                onInput { onChange(state.copy(title = this.value ?: "")) }
            }
        }

        div {
            applyColumn()
            label {
                htmlFor("description")
                +"Description"
            }
            text(state.description) {
                id("description")
                type(InputType.Text)
                onInput { onChange(state.copy(description = this.value ?: "")) }
            }
        }

        div {
            applyColumn()
            label {
                htmlFor("date")
                +"Date"
            }
            text(state.startDateTime?.format(LocalDateTime.Formats.ISO) ?: "") {
                id("date")
                type(InputType.DatetimeLocal)
                onChange {
                    onChange(state.copy(startDateTime = this.value?.let { newValue ->
                        LocalDateTime.parse(
                            newValue,
                            LocalDateTime.Formats.ISO
                        )
                    }))
                }
            }
        }

        div {
            applyColumn()
            label {
                htmlFor("type")
                +"Type"
            }
            select {
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

        div {
            applyColumn()
            label {
                htmlFor("lengthMinutes")
                +"Length (Minutes)"
            }
            text(state.lengthMinutes.toString()) {
                id("lengthMinutes")
                type(InputType.Number)
                attribute("min", "1")
                onInput { onChange(state.copy(lengthMinutes = this.value?.toIntOrNull() ?: 0)) }
            }
        }

        div {
            applyColumn()
            label {
                htmlFor("users")
                +"Users"
            }
            val clients = remember(users) { users.filter { !it.isAdmin } }
            if (clients.isEmpty())
                +"No users available"
            else
                ul {
                    id("users")
                    clients.forEach { user ->
                        li {
                            +"${user.firstName} ${user.lastName}"
                            onClick { onChange(state.copy(participants = state.participants + user)) }
                        }
                    }
                }
        }

        div {
            applyColumn()
            label {
                +"Selected Participants"
            }
            ul {
                state.participants.forEach { participant ->
                    li {
                        +"${participant.firstName} ${participant.lastName}"
                    }
                }
            }
        }

        button("Create Training") {
            onClick {
                onSubmit(state)
            }
        }
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