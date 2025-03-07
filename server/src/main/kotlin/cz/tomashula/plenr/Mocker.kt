package cz.tomashula.plenr

import cz.tomashula.plenr.feature.training.TrainingParticipantTable
import cz.tomashula.plenr.feature.training.TrainingTable
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.user.UserPermanentAvailabilityTable
import cz.tomashula.plenr.feature.user.TempBusyTimeTable
import cz.tomashula.plenr.feature.user.UserPreferencesTable
import cz.tomashula.plenr.feature.user.UserSetPasswordTable
import cz.tomashula.plenr.feature.user.UserTable
import cz.tomashula.plenr.security.PasswordHasher
import cz.tomashula.plenr.util.now
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class Mocker(
    private val db: Database,
    private val passwordHasher: PasswordHasher
)
{
    fun fill()
    {
        transaction(db) {
            // Drop all used tables
            SchemaUtils.drop(
                TrainingParticipantTable,
                UserSetPasswordTable,
                UserPreferencesTable,
                TempBusyTimeTable,
                UserPermanentAvailabilityTable,
                TrainingTable,
                UserTable
            )
            // Recreate all the tables
            SchemaUtils.create(
                UserTable,
                UserPermanentAvailabilityTable,
                TrainingTable,
                TrainingParticipantTable
            )
        }

        createUsers()
        createTrainings()
    }

    private fun createUsers()
    {
        transaction(db) {
            val usersCsv = this::class.java.classLoader.getResource("$MOCK_FOLDER/$USERS_CSV")
                ?.readText()
                ?.trim()
                ?.lines()
                ?: throw IllegalArgumentException("Users CSV file not found")

            // Skip the header line
            usersCsv.drop(1).forEach { line ->
                val fields = line.split(";")
                val firstName = fields[0]
                val lastName = fields[1]
                val email = fields[2]
                val phone = fields[3]
                val isAdmin = fields[4]
                val availableFrom = fields[5]
                val availableTo = fields[6]

                // Hash the password using runBlocking since PasswordHasher is suspending
                val passwordHash = runBlocking {
                    passwordHasher.hash(DEFAULT_PASSWORD)
                }

                // Insert user into UserTable
                val userId = UserTable.insert {
                    it[this.firstName] = firstName
                    it[this.lastName] = lastName
                    it[this.email] = email
                    it[this.phone] = phone
                    it[this.passwordHash] = passwordHash
                    it[this.isAdmin] = isAdmin.toBoolean()
                } get UserTable.id // Retrieve the auto-generated user ID

                // Insert busy times for all days of the week
                DayOfWeek.entries.forEach { dayOfWeek ->
                    UserPermanentAvailabilityTable.insert {
                        it[this.userId] = userId.value
                        it[this.day] = dayOfWeek
                        it[this.start] = LocalTime.parse(availableFrom, timeFormat)
                        it[this.end] = LocalTime.parse(availableTo, timeFormat)
                    }
                }
            }
        }
    }

    private fun createTrainings()
    {
        transaction(db) {
            val trainingsCsv =
                this::class.java.classLoader.getResource("$MOCK_FOLDER/$TRAININGS_CSV")
                    ?.readText()
                    ?.trim()
                    ?.lines()
                    ?: throw IllegalArgumentException("Training CSV file not found")

            // Skip the header line
            trainingsCsv.drop(1).forEach { line ->
                val fields = line.split(";")
                val arrangerEmail = fields[0]
                val name = fields[1]
                val description = fields[2]
                val type = fields[3]
                val startDateOffset = fields[4]
                val startTime = fields[5]
                val lengthMinutes = fields[6]
                val participantsRaw = fields.getOrElse(7) { "" }

                // Find arranger ID using the email with the new `selectAll().where`
                val arrangerId = UserTable.selectAll()
                    .andWhere { UserTable.email eq arrangerEmail }
                    .single()[UserTable.id]

                // Calculate the start date for the training
                val startDate = LocalDate.now()
                    .plus(startDateOffset.toInt(), DateTimeUnit.DAY)
                val startDateTime = startDate.atTime(LocalTime.parse(startTime, timeFormat))

                // Insert training into TrainingTable
                val trainingId = TrainingTable.insert {
                    it[this.arrangerId] = arrangerId
                    it[this.name] = name
                    it[this.description] = description
                    it[this.type] = TrainingType.entries[type.toInt()] // Convert the type string to enum
                    it[this.startDateTime] = startDateTime
                    it[this.lengthMinutes] = lengthMinutes.toInt()
                } get TrainingTable.id

                // Insert participants into TrainingParticipantTable
                val participantEmails = participantsRaw.split(",")
                participantEmails.forEach { participantEmail ->
                    val participantId = UserTable.selectAll()
                        .andWhere { UserTable.email eq participantEmail }
                        .single()[UserTable.id]

                    TrainingParticipantTable.insert {
                        it[this.trainingId] = trainingId.value
                        it[this.participantId] = participantId.value
                    }
                }
            }
        }
    }

    companion object
    {
        private val timeFormat = LocalTime.Format {
            hour(padding = Padding.ZERO)
            char(':')
            minute(padding = Padding.ZERO)
        }

        private const val MOCK_FOLDER = "mock"
        private const val USERS_CSV = "users.csv"
        private const val TRAININGS_CSV = "trainings.csv"
        private const val DEFAULT_PASSWORD = "password"
    }
}
