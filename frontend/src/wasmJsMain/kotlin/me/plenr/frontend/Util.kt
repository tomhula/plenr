package me.plenr.frontend

import kotlinx.browser.window

fun getCurrentPort() = if (window.location.port.isNotEmpty())
    window.location.port.toInt()
else
    if (window.location.protocol == "https:")
        443
    else
        80