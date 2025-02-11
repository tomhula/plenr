package me.plenr.frontend

import androidx.compose.runtime.*
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.*
import me.plenr.frontend.component.materialIconOutlined
import me.plenr.frontend.page.admin.addUserPage
import me.plenr.frontend.page.admin.adminHomePage
import me.plenr.frontend.page.admin.arrangeTrainingsPage
import me.plenr.frontend.page.admin.manageUsersPage
import me.plenr.frontend.page.adminsetup.adminSetupPage
import me.plenr.frontend.page.login.loginPage
import me.plenr.frontend.page.passwordsetup.passwordSetupPage
import me.plenr.frontend.page.user.userPreferencesPage
import me.plenr.frontend.page.userHomePage

class PlenrFrontendApp : Application()
{
    override fun start(state: String?)
    {
        root("root") {
            root("root") {
                val viewModel = remember { MainViewModel() }
                var router: Router? = null

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
                                append(" - ${user.firstName} ${user.lastName}")
                                if (user.isAdmin)
                                    append(" (Admin)")
                            }
                        }
                        +header
                        if (viewModel.isLoggedIn)
                            button(className = "icon-button logout-button") {
                                onClick {
                                    viewModel.logout()
                                    router!!.navigate("/login")
                                }

                                materialIconOutlined("logout")
                            }
                    }

                    main {
                        BrowserRouter(initPath) {
                            router = Router.current
                            LaunchedEffect(Unit) {
                                if (!viewModel.adminExists())
                                    router.navigate("/admin-setup")
                            }
                            route("/") {
                                if (viewModel.user?.isAdmin == true)
                                    adminHomePage(viewModel)
                                else
                                    userHomePage(viewModel)
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
                            route("/preferences") {
                                userPreferencesPage(viewModel)
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
                }
                else
                {
                    span("spinner")
                }
            }
        }
    }

    /* To retain state during hot reload, this function can return anything (e.g. state serialized to json)
    * And it will be passed to start in state parameter */
    override fun dispose(): String? = null
}
