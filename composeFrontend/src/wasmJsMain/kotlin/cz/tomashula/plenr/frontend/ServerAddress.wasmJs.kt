package cz.tomashula.plenr.frontend

import io.ktor.http.URLProtocol
import kotlinx.browser.window

actual fun getServerAddress(): String
{
    return window.location.hostname
}

actual fun getServerPort(): Int
{
    return getCurrentPort().let { if (it == 3000) 8080 else it } /* Webpack dev server detection */
}

actual fun getServerProtocol(): URLProtocol
{
    return if (window.location.protocol == "https:") URLProtocol.WSS else URLProtocol.WS
}

private fun getCurrentPort() = if (window.location.port.isNotEmpty())
    window.location.port.toInt()
else
    if (window.location.protocol == "https:")
        443
    else
        80
