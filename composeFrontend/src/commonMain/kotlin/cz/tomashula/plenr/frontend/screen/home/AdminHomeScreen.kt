package cz.tomashula.plenr.frontend.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cz.tomashula.plenr.frontend.ui.component.TrainingCalendar

@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeScreenViewModel,
    onManageUsersClick: () -> Unit = {},
    onArrangeTrainingsClick: () -> Unit = {},
)
{
    val uiState = viewModel.uiState

    Column {
        Row {
            Button(onClick = onManageUsersClick) {
                Text("Manage Users")
            }
            Button(onClick = onArrangeTrainingsClick) {
                Text("Arrange Trainings")
            }
        }
        TrainingCalendar(
            selectedWeek = uiState.selectedWeek,
            viewer = viewModel.user,
            onWeekChange = viewModel::onWeekChange,
            trainings = uiState.trainings,
            onTrainingClick = viewModel::onTrainingClick
        )
    }
}
