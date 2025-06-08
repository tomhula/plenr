package cz.tomashula.plenr.frontend.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxScope
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.ui.Colors
import kotlinx.datetime.*

/**
 * A training card.
 *
 * @param viewer The viewer, which is excluded from the participant list.
 */
@Composable
fun Training(
    training: TrainingWithParticipantsDto,
    viewer: UserDto? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val participants = remember(training.participants) { training.participants.filter { it.id != viewer?.id } }
    val backgroundColor = if (training.type == TrainingType.DRESSAGE) 
        Colors.DRESSAGE_TRAINING_BACKGROUND else Colors.PARKOUR_TRAINING_BACKGROUND

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(5.dp)
    ) {
        content()

        Column {
            Text(
                text = training.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textDecoration = if (training.cancelled) TextDecoration.LineThrough else TextDecoration.None
            )

            FlowRow(
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp, start = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                participants.forEach { participant ->
                    ParticipantBadge(participant = participant)
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val startTime = remember(training.startDateTime) {
                    val hour = training.startDateTime.time.hour.toString().padStart(2, '0')
                    val minute = training.startDateTime.time.minute.toString().padStart(2, '0')
                    "$hour:$minute"
                }

                val endTime = remember(training.startDateTime, training.lengthMinutes) {
                    val timeZone = TimeZone.currentSystemDefault()
                    val endDateTime = training.startDateTime.toInstant(timeZone)
                        .plus(training.lengthMinutes, DateTimeUnit.MINUTE)
                        .toLocalDateTime(timeZone)
                    val hour = endDateTime.time.hour.toString().padStart(2, '0')
                    val minute = endDateTime.time.minute.toString().padStart(2, '0')
                    "$hour:$minute"
                }

                Text(
                    text = startTime,
                    fontSize = 12.sp
                )

                Text(
                    text = endTime,
                    fontSize = 12.sp
                )
            }
        }
    }
}
