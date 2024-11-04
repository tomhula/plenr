package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.form.Form
import web.dom.events.Event

@Composable
fun Form<*>.onSubmit(preventDefault: Boolean = true, block: (Event) -> Unit)
{
    onEvent<Event>("submit") { event ->
        if (preventDefault)
            event.preventDefault()
        block(event)
    }
}