package cz.tomashula.plenr.frontend

import androidx.compose.runtime.Composable
import dev.kilua.form.text.IText
import web.window.window

fun getCurrentPort() = if (window.location.port.isNotEmpty())
    window.location.port.toInt()
else
    if (window.location.protocol == "https:")
        443
    else
        80

@Composable
fun IText.allowLettersOnly()
{
    onInput {
        this.value = this.value?.filter { it.isLetter() }
    }
}