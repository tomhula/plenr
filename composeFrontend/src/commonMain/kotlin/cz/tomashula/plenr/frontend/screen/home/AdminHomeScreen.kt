package cz.tomashula.plenr.frontend.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cz.tomashula.plenr.frontend.ui.component.TrainingCalendar

@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeScreenViewModel
) {
    val uiState = viewModel.uiState

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            TrainingCalendar(
                selectedWeek = uiState.selectedWeek,
                viewer = viewModel.user,
                onWeekChange = viewModel::onWeekChange,
                trainings = uiState.trainings,
                onTrainingClick = viewModel::onTrainingClick
            )
        }
    }
}
