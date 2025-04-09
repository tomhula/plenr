package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.*
import web.html.HTMLTableCellElement

@Composable
inline fun IComponent.plenrTable(
    crossinline content: @Composable ITable.() -> Unit
)
{
    table {
        background(Background(Color("#F3F3F3")))
        content()
    }
}

@Composable
inline fun <T : ITag<HTMLTableCellElement>> IComponent.plenrTableCell(
    crossinline content: @Composable T.() -> Unit,
    cell: @Composable (@Composable T.() -> Unit) -> Unit
)
{
    cell {
        border(Border(1.px, BorderStyle.Solid, Color("#CCCCCC")))
        padding(10.px)
        textAlign(TextAlign.Center)
        content()
    }
}

@Composable
inline fun IComponent.plenrTh(
    crossinline content: @Composable ITh.() -> Unit
) = plenrTableCell(content = content, cell = { th { it() } })

@Composable
inline fun IComponent.plenrTd(
    crossinline content: @Composable ITd.() -> Unit
) = plenrTableCell(content = content, cell = { td { it() } })
