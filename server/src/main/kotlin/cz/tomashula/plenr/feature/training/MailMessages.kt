package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.feature.user.UserDto
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

object MailMessages
{
    fun getNewTrainingsMessage(
        arranger: UserDto,
        target: UserDto,
        trainings: Set<TrainingWithParticipantsDto>,
        serverUrl: String
    ): MailMessage = MailMessage(
        subject = "You have been arranged ${trainings.size} trainings",
        body = buildString {
            appendLine("You have the following trainings with ${arranger.fullName}:")
            for (training in trainings.sortedBy { it.startDateTime })
            {
                append(training.startDateTime.format(dateTimeFormat))
                appendLine(":")
                val indent = "  "
                appendLine(indent + "${training.lengthMinutes} minutes")
                appendLine(indent + "Name: ${training.name}")
                appendLine(indent + "Description: ${training.description}")
                appendLine(indent + "Type: ${training.type}")
                if (training.participants.isNotEmpty())
                    appendLine(indent + "With: " + training.participants.filterNot { it.id == target.id }.joinToString { it.fullName })

                appendLine(serverUrl)
            }
        }
    )

    private val dateTimeFormat = LocalDateTime.Format {
        dayOfMonth()
        char('.')
        monthNumber()
        chars(". ")
        hour()
        char(':')
        minute()
    }
}

data class MailMessage(
    val subject: String,
    val body: String
)
