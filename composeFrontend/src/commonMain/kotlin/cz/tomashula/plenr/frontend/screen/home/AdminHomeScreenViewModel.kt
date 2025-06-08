package cz.tomashula.plenr.frontend.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.AppViewModel
import cz.tomashula.plenr.util.Week
import kotlinx.coroutines.launch

class AdminHomeScreenViewModel(
    private val appViewModel: AppViewModel
) : ViewModel()
{
    var uiState by mutableStateOf(AdminHomeScreenState())
        private set
    
    val user: UserDto
        get() = appViewModel.user!!
    
    private var oldestFetchedWeek: Week? = null
    
    init
    {
        viewModelScope.launch {
            loadTrainings()
        }
    }

    fun onWeekChange(week: Week)
    {
        uiState = uiState.copy(selectedWeek = week)
        loadTrainings()
    }

    fun onTrainingClick(training: TrainingWithParticipantsDto)
    {
    }

    private fun loadTrainings()
    {
        viewModelScope.launch {
            try
            {
                val week = uiState.selectedWeek
                
                if (oldestFetchedWeek == null || week < oldestFetchedWeek!!)
                {
                    val from = week.dateTimeRange.start
                    val to = null
                    
                    val newTrainings = appViewModel.getAllTrainingsAdmin(from, to)
                    uiState = uiState.copy(
                        trainings = uiState.trainings + newTrainings
                    )
                    oldestFetchedWeek = week
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }
}
