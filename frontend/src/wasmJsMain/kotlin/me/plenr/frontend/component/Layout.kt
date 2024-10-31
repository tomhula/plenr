package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.*

@Composable
fun IComponent.column(
    justifyContent: JustifyContent = JustifyContent.Start,
    alignItems: AlignItems = AlignItems.Start,
    content: @Composable IDiv.() -> Unit
) = div {
    display(Display.Flex)
    flexDirection(FlexDirection.Column)
    justifyContent(justifyContent)
    alignItems(alignItems)
    content()
}

@Composable
fun IComponent.row(
    justifyContent: JustifyContent = JustifyContent.Center,
    alignItems: AlignItems = AlignItems.Center,
    content: @Composable IDiv.() -> Unit
) = div {
    display(Display.Flex)
    flexDirection(FlexDirection.Row)
    justifyContent(justifyContent)
    alignSelf(alignItems)
    content()
}