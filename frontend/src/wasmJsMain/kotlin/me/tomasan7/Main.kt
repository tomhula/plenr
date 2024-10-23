package me.tomasan7

import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t
import dev.kilua.startApplication

class PlenrFrontendApp : Application()
{
    override fun start()
    {
        root("root") {
            h1t(text = "Hello, Plenr!")
        }
    }
}

fun main()
{
    startApplication(::PlenrFrontendApp)
}