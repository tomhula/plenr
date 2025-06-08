package cz.tomashula.plenr.frontend.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.ui.Colors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ParticipantBadge(
    participant: UserDto,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Colors.getColor(participant.id))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = participant.fullName,
            color = Color.Black
        )
    }
}
