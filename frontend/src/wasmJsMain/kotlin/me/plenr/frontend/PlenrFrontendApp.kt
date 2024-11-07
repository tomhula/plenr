package me.plenr.frontend

import androidx.compose.runtime.*
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import app.softwork.routingcompose.navigate
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.*
import me.plenr.frontend.page.admin.addUserPage
import me.plenr.frontend.page.admin.adminHomePage
import me.plenr.frontend.page.admin.arrangeTrainingsPage
import me.plenr.frontend.page.admin.manageUsersPage
import me.plenr.frontend.page.adminsetup.adminSetupPage
import me.plenr.frontend.page.homePage
import me.plenr.frontend.page.login.loginPage
import me.plenr.frontend.page.passwordsetup.passwordSetupPage

class PlenrFrontendApp : Application()
{
    override fun start()
    {
        root("root") {
            val viewModel = remember { MainViewModel() }

            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.init()
                initialized = true
            }

            if (initialized)
            {
                val initPath = if (viewModel.isLoggedIn)
                    "/"
                else
                    "/login"

                header(id = "main-header") {
                    val header = buildString {
                        append("Plenr")
                        viewModel.user?.let { user ->
                            append(" - ${user.firstName}")
                            if (user.isAdmin)
                                append(" (Admin)")
                        }
                    }
                    +header
                }

                BrowserRouter(initPath) {
                    val router = Router.current
                    LaunchedEffect(Unit) {
                        if (!viewModel.adminExists())
                            router.navigate("/admin-setup")
                    }
                    route("/") {
                        if (viewModel.user?.isAdmin == true)
                            adminHomePage(viewModel)
                        else
                            homePage(viewModel)
                    }
                    route("/admin-setup") {
                        adminSetupPage(viewModel)
                    }
                    route("/set-password") {
                        string { token ->
                            passwordSetupPage(viewModel, token)
                        }
                    }
                    route("/login") {
                        loginPage(viewModel)
                    }
                    if (viewModel.user?.isAdmin == true)
                    {
                        route("/manage-users") {
                            manageUsersPage(viewModel)
                        }
                        route("/arrange-trainings") {
                            arrangeTrainingsPage(viewModel)
                        }
                        route("/add-user") {
                            addUserPage(viewModel)
                        }
                    }
                    noMatch {
                        h1t(text = "Not Found")
                    }
                }
            }
            else
            {
                span("spinner")
            }
        }
    }
}