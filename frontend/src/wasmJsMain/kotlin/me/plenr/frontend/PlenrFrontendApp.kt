package me.plenr.frontend

import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t

class PlenrFrontendApp : Application()
{
    override fun start()
    {
        root("root") {
            h1t(text = "Hello, Plenr!")
        }
    }
}