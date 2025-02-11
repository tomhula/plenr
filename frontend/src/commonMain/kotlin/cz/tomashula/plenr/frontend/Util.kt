package cz.tomashula.plenr.frontend

import web.window

fun getCurrentPort() = if (window.location.port.isNotEmpty())
    window.location.port.toInt()
else
    if (window.location.protocol == "https:")
        443
    else
        80
