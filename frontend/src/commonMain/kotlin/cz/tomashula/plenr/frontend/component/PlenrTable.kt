package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.Background
import dev.kilua.html.Border
import dev.kilua.html.BorderStyle
import dev.kilua.html.Color
import dev.kilua.html.ITable
import dev.kilua.html.ITag
import dev.kilua.html.ITd
import dev.kilua.html.ITh
import dev.kilua.html.TextAlign
import dev.kilua.html.px
import dev.kilua.html.table
import dev.kilua.html.th
import dev.kilua.html.td
import web.dom.HTMLTableCellElement

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
