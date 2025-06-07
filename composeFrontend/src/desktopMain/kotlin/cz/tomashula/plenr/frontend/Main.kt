package cz.tomashula.plenr.frontend

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ComposeWebTest",
    ) {
        App()
    }
}
