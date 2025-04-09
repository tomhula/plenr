package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.Colors
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Modifier
import dev.kilua.compose.ui.fillMaxWidth
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.panel.hPanel
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * A training card.
 *
 * @param viewer The viewer, which is excluded from the participants list.
 */
@Composable
fun IComponent.training(
    training: TrainingWithParticipantsDto,
    viewer: UserDto? = null,
    onClick: () -> Unit,
    content: @Composable IDiv.() -> Unit = {}
)
{
    val participants = remember(training.participants) { training.participants.filter { it.id != viewer?.id } }

    div {
        onClick { onClick() }
        background(if (training.type == TrainingType.DRESSAGE) Colors.DRESSAGE_TRAINING_BACKGROUND else Colors.PARKOUR_TRAINING_BACKGROUND)
        padding(5.px)
        fontSize(0.8.rem)
        borderRadius(5.px)
        cursor(Cursor.Pointer)
        style("pointer-events", "auto")
        content()

        Column {
            spant(training.name) {
                fontWeight(FontWeight.Bold)
                if (training.cancelled) {
                    style("text-decoration", "line-through")
                }
            }

            hPanel(
                flexWrap = FlexWrap.Wrap,
                gap = 4.px
            ) {
                marginTop(5.px)
                marginBottom(5.px)
                marginLeft(5.px)
                for (participant in participants)
                    participantBadge(participant)
            }

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

@Composable
fun IComponent.trainingDialogBody(
    training: TrainingWithParticipantsDto,
    viewer: UserDto? = null,
)
{
    val participants = remember(training.participants) { training.participants.filter { it != viewer} }

    @Composable
    fun property(name: String, value: String) {
        div {
            marginTop(16.px)
            bt(name)
            br()
            spant(value) {
                marginTop(5.px)
            }
        }
    }

    property("Name", training.name)
    if (training.cancelled) {
        div {
            marginTop(5.px)
            spant("This training is cancelled") {
                color(dev.kilua.html.Color.Red)
                fontWeight(FontWeight.Bold)
            }
        }
    }
    property("Type", training.type.name.lowercase().replaceFirstChar { it.uppercase() })
    property("Description", training.description)
    property("Start", training.startDateTime.format(dateTimeFormat))
    property("Length", "${training.lengthMinutes}min")
    div {
        marginTop(10.px)
        if (viewer == null || viewer.isAdmin)
            bt("Participants")
        else
        {
            if (participants.isEmpty())
                bt("You have the training alone with the trainer.")
            else
                bt("With")
        }
        br()
        for (participant in participants)
            participantBadge(participant) {
                marginLeft(5.px)
            }
    }
}

private val dateTimeFormat = LocalDateTime.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    chars(". ")
    hour()
    char(':')
    minute()
}

private val timeFormat = LocalTime.Format {
    hour(padding = Padding.ZERO)
    char(':')
    minute(padding = Padding.ZERO)
}
