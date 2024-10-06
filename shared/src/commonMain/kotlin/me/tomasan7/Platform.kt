package me.tomasan7

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform