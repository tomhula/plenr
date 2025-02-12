package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.ISpan
import dev.kilua.html.spant
import kotlinx.serialization.json.JsonNull.content

@Composable
fun IComponent.materialIconOutlined(
    name: String,
    content: @Composable ISpan.() -> Unit = {}
)
{
    spant(
        className = "material-symbols-outlined",
        text = name,
        content =  content
    )
}
