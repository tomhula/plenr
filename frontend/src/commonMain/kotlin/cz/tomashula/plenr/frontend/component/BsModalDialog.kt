package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.kilua.core.IComponent
import dev.kilua.modal.IModal
import dev.kilua.modal.modal
import web.events.Event

@Composable
fun IComponent.bsModalDialog(
    shown: Boolean,
    title: String,
    onDismiss: () -> Unit,
    footer: @Composable IComponent.() -> Unit = {},
    body: @Composable IModal.() -> Unit
)
{
    modal(
        caption = title,
        closeButtonAction = { onDismiss() },
        centered = true,
        scrollable = true,
        escape = true,
    )
    {
        onEvent<Event>("hidden.bs.modal") {
            onDismiss()
        }
        LaunchedEffect(shown) {
            if (shown)
                show()
            else
                hide()
        }

        body()
        footer(footer)
    }
}
