package cz.tomashula.plenr.frontend.page.user

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.check.checkBox
import dev.kilua.form.form
import dev.kilua.form.text.text
import dev.kilua.html.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.component.applyColumn
import cz.tomashula.plenr.frontend.component.applyRow
import cz.tomashula.plenr.frontend.component.formField
import cz.tomashula.plenr.frontend.component.materialIconOutlined
import cz.tomashula.plenr.frontend.component.onSubmit
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.util.rangeTo
import web.document
import web.window

@Composable
fun IComponent.userPreferencesPage(viewModel: MainViewModel)
{
    val router = Router.current
    val coroutineScope = rememberCoroutineScope()
    var preferences by remember { mutableStateOf<UserPreferencesDto?>(null) }
    var permanentBusyTimes by remember { mutableStateOf<WeeklyTimeRanges?>(null) }
    var addBusyTimeDialogDay by remember { mutableStateOf(DayOfWeek.MONDAY) }

    if (viewModel.user?.isAdmin == true)
    {
        h2t("Admins do not have any preferences") {
          textAlign(TextAlign.Center)
            marginTop(50.px)
        }
        return
    }

    LaunchedEffect(Unit) {
        preferences = viewModel.getPreferences()
        permanentBusyTimes = viewModel.getPermanentBusyTimes()
    }

    val addBusyTimeDialog = dialogRef(className = "modal-dialog add-busy-time-dialog") {
        var start by remember { mutableStateOf(LocalTime(0, 0)) }
        var end by remember { mutableStateOf(LocalTime(0, 0)) }

        form {
            onSubmit {
                permanentBusyTimes = permanentBusyTimes?.builder()?.addTimeRange(addBusyTimeDialogDay, start..end)?.build()
                this@dialogRef.element.close("")
            }

            text(type = InputType.Time, value = start.toString()) {
                onChange { start = this.value?.let { LocalTime.parse(it) } ?: LocalTime(0, 0) }
            }

            spant(" - ")

            text(type = InputType.Time, value = end.toString()) {
                onChange { end = this.value?.let { LocalTime.parse(it) } ?: LocalTime(0, 0) }
            }

            button("Add", className = "primary-button", type = ButtonType.Submit)
        }
    }

    form(className = "form") {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        onSubmit {
            coroutineScope.launch {
                val preferences = async { viewModel.setPreferences(preferences!!) }
                val permanentBusyTimes = async { viewModel.setPermanentBusyTimes(permanentBusyTimes!!) }
                awaitAll(preferences, permanentBusyTimes)
                window.alert("Preferences saved")
                router.navigate(Route.HOME)
            }
        }

        formField(
            inputId = "trainings-per-week",
            label = "Trainings per week",
            value = preferences?.trainingsPerWeek?.toString() ?: "Loading...",
            type = InputType.Number,
            onChange = { preferences = preferences?.copy(trainingsPerWeek = it.toInt()) },
            inputBlock = { attribute("min", "1"); attribute("max", "7") }
        )

        div(className = "form-field") {
            applyColumn()
            label(className = "form-field-label") {
                +"Permanent busy times"
            }
            // Here just to trigger recomposition when permanentBusyTimes changes
            if (permanentBusyTimes != null)
                for (day in DayOfWeek.entries)
                    permanentBusyTimesDay(
                        day = day,
                        busyTimes = permanentBusyTimes!!.getRangesForDay(day),
                        onAddClick = {
                            addBusyTimeDialogDay = day
                            addBusyTimeDialog.element.showModal(document.body!!)
                        },
                        onRemoveClick = {
                            permanentBusyTimes = permanentBusyTimes!!.builder().removeTimeRange(day, it).build()
                        }
                    )
        }

        div(className = "form-field") {
            applyColumn()
            label(className = "form-field-label") {
                +"Notifications"
            }

            table(className = "notifications-table") {
                thead {
                    tr {
                        th { +"Notification" }
                        th { +"Email" }
                        th { +"SMS" }
                    }
                }
                tbody {
                    notificationsTableRow(
                        label = "Training Arranged",
                        email = preferences?.trainingArrangedNotiEmail == true,
                        sms = preferences?.trainingArrangedNotiSms == true,
                        onEmailChange = { preferences = preferences?.copy(trainingArrangedNotiEmail = it) },
                        onSmsChange = { preferences = preferences?.copy(trainingArrangedNotiSms = it) }
                    )
                    notificationsTableRow(
                        label = "Training Moved",
                        email = preferences?.trainingMovedNotiEmail == true,
                        sms = preferences?.trainingMovedNotiSms == true,
                        onEmailChange = { preferences = preferences?.copy(trainingMovedNotiEmail = it) },
                        onSmsChange = { preferences = preferences?.copy(trainingMovedNotiSms = it) }
                    )
                    notificationsTableRow(
                        label = "Training Cancelled",
                        email = preferences?.trainingCancelledNotiEmail == true,
                        sms = preferences?.trainingCancelledNotiSms == true,
                        onEmailChange = { preferences = preferences?.copy(trainingCancelledNotiEmail = it) },
                        onSmsChange = { preferences = preferences?.copy(trainingCancelledNotiSms = it) }
                    )
                }
            }
        }

        button(label = "Save preferences", type = ButtonType.Submit, className = "primary-button")
    }
}

@Composable
private fun IComponent.notificationsTableRow(
    label: String,
    email: Boolean,
    sms: Boolean,
    onEmailChange: (Boolean) -> Unit,
    onSmsChange: (Boolean) -> Unit
)
{
    tr {
        th { +label }
        td {
            checkBox(value = email) {
                onChange { onEmailChange(this.value) }
            }
        }
        td {
            checkBox(value = sms) {
                onChange { onSmsChange(this.value) }
            }
        }
    }
}

@Composable
private fun IComponent.permanentBusyTimesDay(
    day: DayOfWeek,
    busyTimes: List<ClosedRange<LocalTime>>,
    onAddClick: () -> Unit,
    onRemoveClick: (ClosedRange<LocalTime>) -> Unit
)
{
    div {
        applyRow(alignItems = AlignItems.Center)
        spant(day.toString().lowercase().replaceFirstChar(Char::uppercase))
        button(className = "icon-button") {
            onClick {
                onAddClick()
            }
            materialIconOutlined("add")
        }
    }

    div {
        applyRow()
        flexWrap(FlexWrap.Wrap)

        for (busyTime in busyTimes)
            spant("${busyTime.start}-${busyTime.endInclusive}", className = "busy-time-range-chip") {
                onClick {
                    onRemoveClick(busyTime)
                }
            }
    }
}
