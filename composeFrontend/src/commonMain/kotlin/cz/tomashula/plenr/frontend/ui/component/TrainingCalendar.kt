package cz.tomashula.plenr.frontend.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.util.Week
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char

@Composable
fun TrainingCalendar(
    selectedWeek: Week,
    viewer: UserDto? = null,
    onWeekChange: (Week) -> Unit = {},
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit = {}
) {
    val trainingsByDate = remember(trainings) { trainings.groupBy { it.startDateTime.date } }
    val upcomingTrainingDaysOrdered = remember(trainingsByDate, selectedWeek) {
        trainingsByDate.keys.filter { it >= selectedWeek.dateTimeRange.endInclusive.date }.sortedBy { it }
    }
    val currentWeek = remember { Week.current() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ArrowSelector(
            selectedItem = selectedWeek,
            onNext = { onWeekChange(selectedWeek.relative(1)) },
            onPrevious = { onWeekChange(selectedWeek.relative(-1)) },
            itemDisplay = {
                when (currentWeek.differenceInWeeks(selectedWeek)) {
                    0 -> "This week"
                    1 -> "Next week"
                    -1 -> "Last week"
                    else -> selectedWeek.toString(dateFormat)
                }
            },
            modifier = Modifier
                .width(250.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (day in selectedWeek.days) {
                TrainingCalendarDay(
                    date = day,
                    viewer = viewer,
                    trainings = trainingsByDate[day]?.toSet() ?: emptySet(),
                    onTrainingClick = onTrainingClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = "Upcoming",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            for (upcomingDay in upcomingTrainingDaysOrdered) {
                TrainingCalendarDay(
                    date = upcomingDay,
                    viewer = viewer,
                    trainings = trainingsByDate[upcomingDay]?.toSet() ?: emptySet(),
                    onTrainingClick = onTrainingClick,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}

@Composable
private fun TrainingCalendarDay(
    date: LocalDate,
    viewer: UserDto? = null,
    trainings: Set<TrainingWithParticipantsDto>,
    onTrainingClick: (TrainingWithParticipantsDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val trainingsOrdered = remember(trainings) { trainings.sortedBy { it.startDateTime } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
            fontWeight = FontWeight.Bold
        )

        Text(
            text = date.format(dateFormat),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        for (training in trainingsOrdered) {
            Training(
                training = training,
                viewer = viewer,
                onClick = { onTrainingClick(training) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private val dateFormat = LocalDate.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
}
