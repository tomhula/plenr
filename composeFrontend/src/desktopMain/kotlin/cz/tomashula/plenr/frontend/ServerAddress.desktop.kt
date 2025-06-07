package cz.tomashula.plenr.frontend

import io.ktor.http.URLProtocol

actual fun getServerAddress(): String
{
    return "localhost"
}

actual fun getServerPort(): Int
{
    return 8080
}

actual fun getServerProtocol(): URLProtocol
{
    return URLProtocol.WS
}
