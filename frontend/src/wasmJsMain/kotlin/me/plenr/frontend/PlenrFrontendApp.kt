package me.plenr.frontend

import androidx.compose.runtime.LaunchedEffect
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t
import me.plenr.frontend.page.adminsetup.adminSetupPage

class PlenrFrontendApp : Application()
{
    private val plenrClient = PlenrClient()

    override fun start()
    {
        root("root") {
            BrowserRouter("/") {
                val router = Router.current
                LaunchedEffect(Unit) {
                    if (!plenrClient.adminExists())
                        router.navigate("/admin-setup")
                }
                route("/") {
                    h1t(text = "Hello, Plenr!")
                }
                route("/admin-setup") {
                    adminSetupPage(plenrClient)
                }
                noMatch {
                    h1t(text = "Not Found")
                }
            }
        }
    }
}