package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.html.helpers.TagStyleFun.Companion.border
import dev.kilua.html.style.pClass
import dev.kilua.html.style.style
import kotlin.time.Duration.Companion.seconds

@Composable
fun IComponent.outlinedMaterialIconButton(
    iconName: String,
    color: Color? = Color("#616161"),
    onClick: () -> Unit = {}
)
{
    button {
        style {
            display(Display.InlineFlex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            background(Color("transparent"))
            border(style = BorderStyle.None)
            cursor(Cursor.Pointer)
            borderRadius(50.perc)
            transition(Transition(property = "color", 0.3.seconds, "ease"))
            color(color)

            pClass("hover") {
                style("filter", "brightness(50%)")
            }
        }

        onClick { onClick() }
        spant(iconName, className = "material-symbols-outlined")
    }
}
