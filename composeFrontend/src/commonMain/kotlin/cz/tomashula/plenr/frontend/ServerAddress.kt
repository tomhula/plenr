package cz.tomashula.plenr.frontend

import io.ktor.http.URLProtocol

expect fun getServerAddress(): String
expect fun getServerPort(): Int
expect fun getServerProtocol(): URLProtocol
