package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.Colors
import dev.kilua.core.IComponent
import dev.kilua.html.Color
import dev.kilua.html.FontWeight
import dev.kilua.html.IDiv

@Composable
fun IComponent.participantBadge(
    participant: UserDto,
    content: @Composable IDiv.() -> Unit = {}
)
{
    bsBadge(participant.fullName, textColor = Color.White, backgroundColor = Colors.getColorForPerson(participant.fullName)) {
        fontWeight(FontWeight.Normal)
        content()
    }
}
