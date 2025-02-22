package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.panel.hPanel

@Composable
fun <T> IComponent.arrowSelector(
    selectedItem: T,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    itemDisplay: (T) -> String,
)
{
    hPanel(
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.SpaceBetween
    ) {
        style("user-select", "none")
        fontSize(1.5.rem)
        width(370.px)
        materialIconOutlined("chevron_left") {
            fontSize(3.rem)
            cursor(Cursor.Pointer)
            onClick { onPrevious() }
        }
        bt(itemDisplay(selectedItem))
        materialIconOutlined("chevron_right") {
            fontSize(3.rem)
            cursor(Cursor.Pointer)
            onClick { onNext() }
        }
    }
}
