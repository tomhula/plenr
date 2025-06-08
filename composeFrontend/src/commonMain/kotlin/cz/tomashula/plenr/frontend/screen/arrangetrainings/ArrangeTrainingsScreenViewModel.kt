package cz.tomashula.plenr.frontend.screen.arrangetrainings

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.feature.training.CreateOrUpdateTrainingDto
import cz.tomashula.plenr.feature.training.TrainingType
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.AppViewModel
import cz.tomashula.plenr.frontend.ui.component.Training
import cz.tomashula.plenr.util.now
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class ArrangeTrainingsScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel()
{
    var uiState by mutableStateOf(ArrangeTrainingsScreenState(selectedDay = LocalDate.now()))
        private set

    val user: UserDto
        get() = appViewModel.user!!

    init
    {
        loadUsers()
    }

    private fun loadUsers()
    {
        viewModelScope.launch {
            try
            {
                val users = appViewModel.getAllUsers().filterNot { it.isAdmin }
                uiState = uiState.copy(
                    users = users,
                    isLoading = false
                )
                loadUserAvailabilities()
                loadTrainings()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun loadUserAvailabilities()
    {
        val selectedDay = uiState.selectedDay ?: return
        viewModelScope.launch {
            try
            {
                val userAvailabilities = appViewModel.getUsersAvailabilityForDay(
                    uiState.users.map { it.id },
                    selectedDay
                ).mapKeys { entry ->
                    uiState.users.find { it.id == entry.key }!!
                }
                uiState = uiState.copy(
                    userAvailabilities = uiState.userAvailabilities + (selectedDay to userAvailabilities)
                )
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    private fun loadTrainings()
    {
        val selectedDay = uiState.selectedDay ?: return
        viewModelScope.launch {
            try
            {
                val trainings = appViewModel.getAllTrainingsAdmin(
                    from = selectedDay.atTime(0, 0),
                    to = selectedDay.atTime(23, 59)
                ).map { it.toTrainingView() }
                uiState = uiState.copy(
                    trainings = uiState.trainings + (selectedDay to trainings)
                )
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun onDayChange(day: LocalDate)
    {
        uiState = uiState.copy(selectedDay = day)
        if (!uiState.userAvailabilities.containsKey(day))
            loadUserAvailabilities()
        if (!uiState.trainings.containsKey(day))
            loadTrainings()
    }

    fun onTrainingClick(training: TrainingWithParticipantsDto)
    {
        uiState = uiState.copy(
            currentDialogTraining = training,
            isCurrentDialogTrainingEdit = true
        )
    }

    fun onNewTrainingClick(dateTime: LocalDateTime)
    {
        val newTraining = TrainingWithParticipantsDto(
            id = -1,
            arranger = user,
            name = "",
            description = "",
            type = TrainingType.DRESSAGE,
            startDateTime = dateTime,
            lengthMinutes = 60,
            participants = emptySet()
        )
        uiState = uiState.copy(
            currentDialogTraining = newTraining,
            isCurrentDialogTrainingEdit = false
        )
    }

    fun onDialogDismiss()
    {
        uiState = uiState.copy(
            currentDialogTraining = null
        )
    }

    fun onDialogSave(training: TrainingWithParticipantsDto)
    {
        val originalTraining = uiState.currentDialogTraining ?: return
        val originalDate = originalTraining.startDateTime.date
        val saveDate = training.startDateTime.date

        // Update trainings map
        val updatedTrainings = uiState.trainings.toMutableMap()

        // If editing, remove the original training
        if (uiState.isCurrentDialogTrainingEdit)
        {
            updatedTrainings[originalDate] = updatedTrainings[originalDate]?.filterNot {
                it.training.id == originalTraining.id
            } ?: emptyList()
        }

        // Add the new/updated training
        updatedTrainings[saveDate] = (updatedTrainings[saveDate] ?: emptyList()) + training.toTrainingView(
            created = !uiState.isCurrentDialogTrainingEdit,
            edited = uiState.isCurrentDialogTrainingEdit
        )

        uiState = uiState.copy(
            trainings = updatedTrainings,
            currentDialogTraining = null
        )
    }

    fun onDialogRemove()
    {
        val training = uiState.currentDialogTraining ?: return
        // Only allow removing trainings that haven't been saved to backend yet
        if (training.id == -1)
        {
            val date = training.startDateTime.date
            val updatedTrainings = uiState.trainings.toMutableMap()
            updatedTrainings[date] = updatedTrainings[date]?.filterNot {
                it.training.id == -1 && it.created
            } ?: emptyList()

            uiState = uiState.copy(
                trainings = updatedTrainings,
                currentDialogTraining = null
            )
        }
    }

    fun saveTrainings()
    {
        viewModelScope.launch {
            try
            {
                val newOrModifiedTrainings = uiState.trainings.values.flatten()
                    .filter { it.edited || it.created }
                    .map { it.training.toCreateTrainingDto() }
                    .toSet()

                appViewModel.arrangeTrainings(newOrModifiedTrainings)

                // Update trainings to mark them as not edited/created
                val updatedTrainings = uiState.trainings.mapValues { (_, trainings) ->
                    trainings.map { it.copy(edited = false, created = false) }
                }

                uiState = uiState.copy(
                    trainings = updatedTrainings
                )
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }
}

private fun TrainingWithParticipantsDto.toCreateTrainingDto() = CreateOrUpdateTrainingDto(
    id = if (id == -1) null else id,
    name = name,
    description = description,
    type = type,
    startDateTime = startDateTime,
    lengthMinutes = lengthMinutes,
    participantIds = participants.map { it.id }.toSet(),
    cancelled = cancelled
)
