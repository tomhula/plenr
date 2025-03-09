package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.util.Week
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.ui.Alignment
import dev.kilua.compose.ui.Modifier
import dev.kilua.compose.ui.fillMaxWidth
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.panel.flexPanel
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

@Composable
fun IComponent.trainingCalendar(
    selectedWeek: Week,
    viewer: UserDto? = null,
    onWeekChange: (Week) -> Unit = {},
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit = {}
)
{
    val trainingsByDate = remember(trainings) { trainings.groupBy { it.startDateTime.date } }
    val upcomingTrainingDaysOrdered = remember(trainingsByDate, selectedWeek) {
            trainingsByDate.keys.filter { it >= selectedWeek.dateTimeRange.endInclusive.date }.sortedBy { it }
    }

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
                trainingCalendarDay(
                    date = day,
                    viewer = viewer,
                    trainings = trainingsByDate[day]?.toSet() ?: emptySet(),
                    onTrainingClick = onTrainingClick
                )
        }

        bt("Upcoming") {
            fontSize(1.5.rem)
            fontWeight(FontWeight.Bolder)
        }

        flexPanel(
            alignItems = AlignItems.Start,
            rowGap = 16.px,
            flexDirection = FlexDirection.Row,
            columnGap = 16.px,
            justifyContent = JustifyContent.SpaceEvenly,
            flexWrap = FlexWrap.Wrap
        ) {
            width(100.perc)

            for (upcomingDay in upcomingTrainingDaysOrdered)
                trainingCalendarDay(
                    date = upcomingDay,
                    viewer = viewer,
                    trainings = trainingsByDate[upcomingDay]?.toSet() ?: emptySet(),
                    onTrainingClick = onTrainingClick
                )
        }
    }
}

@Composable
private fun IComponent.trainingCalendarDay(
    date: LocalDate,
    viewer: UserDto? = null,
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit
)
{
    val trainingsOrdered = remember(trainings) { trainings.sortedBy { it.startDateTime} }

    vPanel(
        justifyContent = JustifyContent.Center,
        gap = 2.px
    ) {
        flexGrow(1)
        maxWidth(200.px)

        spant(date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }) {
            fontWeight(FontWeight.Bold)
            alignSelf(AlignItems.Center)
        }
        spant(date.format(dateFormat))  {
            alignSelf(AlignItems.Center)
            marginBottom(10.px)
            marginTop((-5).px)
            fontSize(0.9.rem)
        }
        for (training in trainingsOrdered)
            training(
                training = training,
                viewer = viewer,
                onClick = { onTrainingClick(training) }
            )
    }
}

private val dateFormat = LocalDate.Format {
    dayOfMonth(padding = Padding.NONE)
    char('.')
    monthNumber(padding = Padding.NONE)
    char('.')
}
