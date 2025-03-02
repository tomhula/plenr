package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.AlignItems
import dev.kilua.html.BorderStyle
import dev.kilua.html.Color
import dev.kilua.html.Cursor
import dev.kilua.html.Display
import dev.kilua.html.JustifyContent
import dev.kilua.html.Transition
import dev.kilua.html.button
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.html.helpers.TagStyleFun.Companion.border
import dev.kilua.html.perc
import dev.kilua.html.px
import dev.kilua.html.spant
import dev.kilua.html.style.pClass
import dev.kilua.html.style.style

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
            transition(Transition(property = "color", 0.3, "ease"))
            color(color)

            pClass("hover") {
                style("filter", "brightness(50%)")
            }
        }

        onClick { onClick() }
        spant(iconName, className = "material-symbols-outlined")
    }
}
