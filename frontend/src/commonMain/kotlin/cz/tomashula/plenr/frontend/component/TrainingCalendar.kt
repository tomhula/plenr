package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.util.Week
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Alignment
import dev.kilua.compose.ui.Modifier
import dev.kilua.compose.ui.fillMaxWidth
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.panel.flexPanel
import dev.kilua.panel.hPanel
import dev.kilua.panel.vPanel
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

@Composable
fun IComponent.trainingCalendar(
    selectedWeek: Week,
    onWeekChange: (Week) -> Unit = {},
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit = {}
)
{
    val trainingsByDate by derivedStateOf { trainings.groupBy { it.startDateTime.date } }
    val upcomingTrainingDaysOrdered by derivedStateOf {
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
                    trainings = trainingsByDate[upcomingDay]?.toSet() ?: emptySet(),
                    onTrainingClick = onTrainingClick
                )
        }
    }
}

@Composable
private fun IComponent.trainingCalendarDay(
    date: LocalDate,
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit
)
{
    val trainingsOrdered by derivedStateOf { trainings.sortedBy { it.startDateTime} }

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
                onClick = { onTrainingClick(training) }
            )
    }
}

@Composable
private fun IComponent.training(
    training: TrainingWithParticipantsDto,
    onClick: () -> Unit
)
{
    div {
        onClick { onClick() }
        background(Color.Bisque)
        padding(5.px)
        fontSize(0.8.rem)
        borderRadius(5.px)
        cursor(Cursor.Pointer)
        style("pointer-events", "auto")

        Column {
            spant(training.name) {
                fontWeight(FontWeight.Bold)
            }
            spant(training.type.name.lowercase().replaceFirstChar { it.uppercase() })

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                spant(training.startDateTime.time.format(timeFormat)) {
                    fontSize(0.7.rem)
                }
                val timeZone = TimeZone.currentSystemDefault()
                val endTimeStr = training.startDateTime.toInstant(timeZone).plus(training.lengthMinutes, DateTimeUnit.MINUTE).toLocalDateTime(
                    timeZone
                ).time.format(timeFormat)
                spant(endTimeStr) {
                    fontSize(0.7.rem)
                }
            }
        }
    }
}

private val dateFormat = LocalDate.Format {
    dayOfMonth(padding = Padding.NONE)
    char('.')
    monthNumber(padding = Padding.NONE)
    char('.')
}

private val timeFormat = LocalTime.Format {
    hour(padding = Padding.ZERO)
    char(':')
    minute(padding = Padding.ZERO)
}
