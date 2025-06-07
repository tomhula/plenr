package cz.tomashula.plenr.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedNumber by remember { mutableStateOf(0) }
            
            ArrowSelector(
                selectedItem = selectedNumber,
                onNext = { selectedNumber++ },
                onPrevious = { selectedNumber-- },
                itemDisplay = { it.toString() },
                modifier = Modifier.width(150.dp)
            )
        }
    }
}
