package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import cz.tomashula.plenr.frontend.Colors
import dev.kilua.core.IComponent
import dev.kilua.html.AlignItems
import dev.kilua.html.BorderStyle
import dev.kilua.html.Color
import dev.kilua.html.JustifyContent
import dev.kilua.html.div
import dev.kilua.html.helpers.TagStyleFun.Companion.background
import dev.kilua.html.helpers.TagStyleFun.Companion.border
import dev.kilua.html.px
import dev.kilua.html.spant
import dev.kilua.panel.hPanel

@Composable
fun IComponent.trainingTypeLegend()
{
    hPanel(
        justifyContent = JustifyContent.Center,
        gap = 20.px
    ) {
        padding(10.px)
        border(1.px, BorderStyle.Solid, Color("#ccc"))
        borderRadius(10.px)
        maxWidth(400.px)

        legendItem(
            color = Colors.DRESSAGE_TRAINING_BACKGROUND,
            text = "Dressage"
        )
        legendItem(
            color = Colors.PARKOUR_TRAINING_BACKGROUND,
            text = "Parkour"
        )
    }
}

@Composable
private fun IComponent.legendItem(
    color: Color,
    text: String
)
{
    hPanel(
        alignItems = AlignItems.Center,
        gap = 10.px,
        className = "legend-item"
    ) {
        div {
            width(20.px)
            height(20.px)
            borderRadius(5.px)
            background(color)
        }
        spant(text)
    }
}
