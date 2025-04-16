package cz.tomashula.plenr.frontend.page.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.availability.BusyPeriodDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsForm
import cz.tomashula.plenr.frontend.component.bsModalDialog
import cz.tomashula.plenr.frontend.component.outlinedMaterialIconButton
import cz.tomashula.plenr.util.LocalDateTimePeriod
import cz.tomashula.plenr.util.now
import dev.kilua.core.IComponent
import dev.kilua.form.time.richDateTime
import dev.kilua.form.time.richTime
import dev.kilua.html.*
import dev.kilua.html.style.style
import dev.kilua.panel.flexPanel
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

@Composable
fun IComponent.availabilityPage(viewModel: MainViewModel)
{
    val router = Router.current
    val coroutineScope = rememberCoroutineScope()

    // State to handle "from" and "to" times for each day
    val days: MutableMap<DayOfWeek, Pair<LocalTime, LocalTime>> = remember {
        mutableStateMapOf<DayOfWeek, Pair<LocalTime, LocalTime>>().apply {
            for (weekDay in DayOfWeek.entries)
                this[weekDay] = LocalTime(0, 0) to LocalTime(15, 0)
        }
    }

    // State for busy periods
    val busyPeriods = remember { mutableStateListOf<BusyPeriodDto>() }

    // State for add busy period dialog
    val showAddBusyPeriodDialog = remember { mutableStateOf(false) }
    val newBusyPeriodStart = remember { mutableStateOf<LocalDateTime?>(null) }
    val newBusyPeriodEnd = remember { mutableStateOf<LocalDateTime?>(null) }

    // Date time formatter for displaying busy periods
    val dateTimeFormat = remember {
        LocalDateTime.Format {
            dayOfMonth()
            char('.')
            monthNumber()
            chars(". ")
            hour()
            char(':')
            minute(padding = Padding.ZERO)
        }
    }

    LaunchedEffect(Unit) {
        // Load permanent availability
        val weeklyTimeRanges = viewModel.getUserPermanentAvailability()
        for (day in DayOfWeek.entries)
        {
            val firstRange = weeklyTimeRanges.getRangesForDay(day).firstOrNull() ?: continue
            days[day] = firstRange.start to firstRange.endInclusive
        }

        // Load busy periods
        val now = LocalDateTime.now()
        val futureBusyPeriods = viewModel.getUserBusyPeriods(now)
        busyPeriods.clear()
        busyPeriods.addAll(futureBusyPeriods)
    }

    // Add busy period dialog
    bsModalDialog(
        shown = showAddBusyPeriodDialog.value,
        title = "Add Busy Period",
        onDismiss = { showAddBusyPeriodDialog.value = false },
        footer = {
            bsButton("Cancel", type = ButtonType.Button) {
                onClick { showAddBusyPeriodDialog.value = false }
            }
            bsButton("Add", type = ButtonType.Button) {
                onClick {
                    val start = newBusyPeriodStart.value
                    val end = newBusyPeriodEnd.value
                    if (start != null && end != null) {
                        coroutineScope.launch {
                            viewModel.addBusyPeriod(start, end)

                            // Refresh busy periods
                            val now = LocalDateTime.now()
                            val futureBusyPeriods = viewModel.getUserBusyPeriods(now)
                            busyPeriods.clear()
                            busyPeriods.addAll(futureBusyPeriods)

                            // Reset form and close dialog
                            newBusyPeriodStart.value = null
                            newBusyPeriodEnd.value = null
                            showAddBusyPeriodDialog.value = false
                        }
                    }
                }
            }
        }
    ) {
        div {
            marginBottom(10.px)
            label("Start date and time")
            richDateTime(value = newBusyPeriodStart.value, format = "dd.MM.yyyy HH:mm") {
                onChange {
                    newBusyPeriodStart.value = this@richDateTime.value
                }
            }
        }
        div {
            marginBottom(10.px)
            label("End date and time")
            richDateTime(value = newBusyPeriodEnd.value, format = "dd.MM.yyyy HH:mm") {
                onChange {
                    newBusyPeriodEnd.value = this@richDateTime.value
                }
            }
        }
    }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center
    ) {
        div {
            maxWidth(800.px)
            width(100.perc)

            // Permanent availability section
            h3("Permanent Availability")
            bsForm<Unit>(
                onSubmit = { _, _, _ ->
                    val weeklyTimeRanges = WeeklyTimeRanges.builder().apply {
                        for ((day, timeRange) in days)
                            addTimeRange(day, timeRange.first, timeRange.second)
                    }.build()

                    viewModel.setUserPermanentAvailability(weeklyTimeRanges)
                    router.navigate(Route.HOME)
                }
            ) {
                maxWidth(500.px)

                table("table") {
                    style {
                        style("th, td") {
                            border(Border(1.px, BorderStyle.Solid, Color("#CCCCCC")))
                            padding(10.px)
                            textAlign(TextAlign.Center)
                        }
                    }
                    thead {
                        tr {
                            th { +"Day" }
                            th { +"From" }
                            th { +"To" }
                        }
                    }
                    tbody {
                        for (weekDay in DayOfWeek.entries)
                            tr {
                                th { +weekDay.name.lowercase().replaceFirstChar { it.uppercase() } }

                                td {
                                    richTime(value = days[weekDay]?.first) {
                                        onChange {
                                            days[weekDay] = this@richTime.value!! to days[weekDay]!!.second
                                        }
                                    }
                                }

                                td {
                                    richTime(value = days[weekDay]?.second) {
                                        onChange {
                                            days[weekDay] = days[weekDay]!!.first to this@richTime.value!!
                                        }
                                    }
                                }
                            }
                    }
                }

                bsButton("Save", type = ButtonType.Submit)
            }

            // Busy periods section
            div {
                marginTop(30.px)
                h3("Busy Periods")
                p("Add periods when you are not available")

                div {
                    marginBottom(10.px)
                    bsButton("Add Busy Period", type = ButtonType.Button) {
                        onClick { showAddBusyPeriodDialog.value = true }
                    }
                }

                if (busyPeriods.isEmpty()) {
                    p("No busy periods defined")
                } else {
                    table("table") {
                        style {
                            style("th, td") {
                                border(Border(1.px, BorderStyle.Solid, Color("#CCCCCC")))
                                padding(10.px)
                                textAlign(TextAlign.Center)
                            }
                        }
                        thead {
                            tr {
                                th { +"Start" }
                                th { +"End" }
                                th { +"Actions" }
                            }
                        }
                        tbody {
                            for (busyPeriod in busyPeriods) {
                                tr {
                                    td { +busyPeriod.period.start.format(dateTimeFormat) }
                                    td { +busyPeriod.period.end.format(dateTimeFormat) }
                                    td {
                                        outlinedMaterialIconButton("delete", Color.Red) {
                                            coroutineScope.launch {
                                                viewModel.removeBusyPeriod(busyPeriod.id)
                                                // Remove from list
                                                busyPeriods.remove(busyPeriod)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
