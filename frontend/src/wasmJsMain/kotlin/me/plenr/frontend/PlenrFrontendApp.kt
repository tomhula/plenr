package me.plenr.frontend

import androidx.compose.runtime.*
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t
import me.plenr.frontend.page.adminsetup.adminSetupPage
import me.plenr.frontend.page.passwordsetup.passwordSetupPage

class PlenrFrontendApp : Application()
{
    private val plenrClient = PlenrClient()

    override fun start()
    {
        root("root") {
            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                plenrClient.init()
                initialized = true
            }

            if (initialized)
            {
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
                    route("/set-password") {
                        string { token ->
                            passwordSetupPage(plenrClient, token)
                        }
                    }
                    noMatch {
                        h1t(text = "Not Found")
                    }
                }
            }
            else
            {
                h1t(text = "Loading...")
            }
        }
    }
}