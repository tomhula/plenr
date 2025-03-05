package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.modal.IModal

@Composable
fun <T> IComponent.bsObjectDialog(
    state: ObjectDialogState<T>,
    title: String,
    onDismissRequest: () -> Unit,
    footer: @Composable IComponent.() -> Unit = {},
    content: @Composable IModal.(T) -> Unit
)
{
    bsModalDialog(
        shown = state.shown,
        title = title,
        onDismiss = onDismissRequest,
        footer = footer,
        body = { state.value?.let { content(it)} }
    )
}

@Stable
class ObjectDialogState<T>
{
    var shown by mutableStateOf(false)
    var value by mutableStateOf<T?>(null)

    fun show(value: T)
    {
        this.value = value
        shown = true
    }

    fun hide()
    {
        shown = false
    }
}

@Composable
fun <T> rememberObjectDialogState() = remember { ObjectDialogState<T>() }
