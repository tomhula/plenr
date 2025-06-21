package cz.tomashula.plenr.frontend

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import androidx.navigation.decodeURIComponent
import io.ktor.http.decodeURLPart
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalBrowserHistoryApi
fun main() {
    ComposeViewport(document.body!!) {
        App(
            onNavHostReady = { navController ->
                val initRoute = window.location.hash.substringAfter('#', "")
                when {
                    initRoute.startsWith("set-password/") -> {
                        val tokenEscaped = initRoute.removePrefix("set-password/")
                        val token = tokenEscaped.decodeURLPart()
                        println("TokenEscaped: $tokenEscaped, Token: $token")
                        navController.navigate(PlenrScreen.SetPassword(token))
                    }
                    else -> {
                        navController.navigate(initRoute)
                    }
                }
                window.bindToNavigation(navController) 
            }
        )
    }
}
