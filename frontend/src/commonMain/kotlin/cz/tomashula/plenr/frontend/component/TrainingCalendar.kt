package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cz.tomashula.plenr.util.Week
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Alignment
import dev.kilua.compose.ui.Modifier
import dev.kilua.compose.ui.fillMaxWidth
import dev.kilua.core.IComponent
import dev.kilua.html.AlignItems
import dev.kilua.html.FlexDirection
import dev.kilua.html.FlexWrap
import dev.kilua.html.JustifyContent
import dev.kilua.html.b
import dev.kilua.html.bt
import dev.kilua.html.perc
import dev.kilua.html.px
import dev.kilua.html.spant
import dev.kilua.panel.flexPanel
import dev.kilua.panel.hPanel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

@Composable
fun IComponent.trainingCalendar(
    selectedWeek: Week,
    onWeekChange: (Week) -> Unit = {}
)
{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.px),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val currentWeek = remember { Week.current() }

        arrowSelector(
            selectedItem = selectedWeek,
            onNext = { onWeekChange(selectedWeek.relative(1)) },
            onPrevious = { onWeekChange(selectedWeek.relative(-1)) },
            itemDisplay = {
                when (currentWeek.differenceInWeeks(selectedWeek))
                {
                    0 -> "This week"
                    1 -> "Next week"
                    -1 -> "Last week"
                    else -> selectedWeek.toString(dateFormat)
                }
            }
        )

        hPanel (
            alignItems = AlignItems.Start,
            gap = 16.px,
            justifyContent = JustifyContent.SpaceEvenly,
            flexWrap = FlexWrap.Wrap
        ) {
            width(100.perc)

            for (day in selectedWeek.days)
                trainingCalendarDay(day)
        }

        bt("Upcoming")
    }
}

@Composable
private fun IComponent.trainingCalendarDay(
    date: LocalDate
)
{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        spant(date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }) {
            style("font-weight", "bold")
        }
        spant(date.format(dateFormat))
    }
}

private val dateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
}
