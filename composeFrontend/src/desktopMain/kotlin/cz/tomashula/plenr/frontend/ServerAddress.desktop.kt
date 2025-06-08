package cz.tomashula.plenr.frontend

import io.ktor.http.URLProtocol

actual fun getServerAddress(): String
{
    return "plenr.tomashula.cz"
}

actual fun getServerPort(): Int
{
    return 443
}

actual fun getServerProtocol(): URLProtocol
{
    return URLProtocol.WSS
}
