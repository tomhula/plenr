package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.html.*
import web.html.HTMLElement

@Composable
fun <E : HTMLElement> ITag<E>.applyColumn(
    justifyContent: JustifyContent = JustifyContent.Start,
    alignItems: AlignItems = AlignItems.Start
)
{
    display(Display.Flex)
    flexDirection(FlexDirection.Column)
    justifyContent(justifyContent)
    alignItems(alignItems)
}

@Composable
fun <E : HTMLElement> ITag<E>.applyRow(
    justifyContent: JustifyContent = JustifyContent.Start,
    alignItems: AlignItems = AlignItems.Start
)
{
    display(Display.Flex)
    flexDirection(FlexDirection.Row)
    justifyContent(justifyContent)
    alignItems(alignItems)
}
