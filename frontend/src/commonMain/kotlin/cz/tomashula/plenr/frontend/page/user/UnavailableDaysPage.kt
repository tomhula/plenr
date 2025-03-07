package cz.tomashula.plenr.frontend.page.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.bsForm
import dev.kilua.core.IComponent
import dev.kilua.form.time.richTime
import dev.kilua.html.*
import dev.kilua.html.style.style
import dev.kilua.panel.flexPanel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
fun IComponent.availabilityPage(viewModel: MainViewModel)
{
    val router = Router.current

    // State to handle "from" and "to" times for each day
    val days: MutableMap<DayOfWeek, Pair<LocalTime, LocalTime>> = remember {
        mutableStateMapOf<DayOfWeek, Pair<LocalTime, LocalTime>>().apply {
            for (weekDay in DayOfWeek.entries)
                this[weekDay] = LocalTime(0, 0) to LocalTime(15, 0)
        }
    }

    LaunchedEffect(Unit) {
        val weeklyTimeRanges = viewModel.getUserPermanentAvailability()
        for (day in DayOfWeek.entries)
        {
            val firstRange = weeklyTimeRanges.getRangesForDay(day).firstOrNull() ?: continue
            days[day] = firstRange.start to firstRange.endInclusive
        }
    }

    flexPanel(
        justifyContent = JustifyContent.Center,
        alignItems = AlignItems.Center
    ) {
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
    }
}
