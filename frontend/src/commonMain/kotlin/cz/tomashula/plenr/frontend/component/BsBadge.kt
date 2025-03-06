package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.Color
import dev.kilua.html.IDiv
import dev.kilua.html.divt
import dev.kilua.html.helpers.TagStyleFun.Companion.background

@Composable
fun IComponent.bsBadge(
    text: String,
    rounded: Boolean = false,
    textColor: Color? = null,
    backgroundColor: Color? = Color.Black,
    content: @Composable IDiv.() -> Unit = {}
)
{
    divt(text, if (rounded) "badge rounded-pill" else "badge") {
        textColor?.let { color(it) }
        backgroundColor?.let { background(it) }
        content()
    }
}
