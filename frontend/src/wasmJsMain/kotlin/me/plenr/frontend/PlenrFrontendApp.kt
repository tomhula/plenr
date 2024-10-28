package me.plenr.frontend

import app.softwork.routingcompose.BrowserRouter
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t
import me.plenr.frontend.page.AdminSetupPage

class PlenrFrontendApp : Application()
{
    override fun start()
    {
        root("root") {
            BrowserRouter("/") {
                route("/") {
                    h1t(text = "Hello, Plenr!")
                }
                route("/admin-setup") {
                    AdminSetupPage()
                }
                noMatch {
                    h1t(text = "Not Found")
                }
            }
        }
    }
}