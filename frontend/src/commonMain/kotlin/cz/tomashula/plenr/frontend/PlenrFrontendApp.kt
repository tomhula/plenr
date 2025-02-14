package cz.tomashula.plenr.frontend

import androidx.compose.runtime.*
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.frontend.component.plenrHeader
import cz.tomashula.plenr.frontend.page.admin.addUserPage
import cz.tomashula.plenr.frontend.page.admin.adminHomePage
import cz.tomashula.plenr.frontend.page.admin.arrangeTrainingsPage
import cz.tomashula.plenr.frontend.page.admin.manageUsersPage
import cz.tomashula.plenr.frontend.page.adminsetup.adminSetupPage
import cz.tomashula.plenr.frontend.page.login.loginPage
import cz.tomashula.plenr.frontend.page.passwordsetup.passwordSetupPage
import cz.tomashula.plenr.frontend.page.user.userPreferencesPage
import cz.tomashula.plenr.frontend.page.userHomePage
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.h1t
import dev.kilua.html.main
import dev.kilua.html.span
import dev.kilua.useModule

class PlenrFrontendApp : Application()
{
    init
    {
        useModule(Logo)
    }

    override fun start(state: String?)
    {
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

                if (viewModel.isLoggedIn)
                    plenrHeader(
                        title = viewModel.user?.fullName ?: "Unknown User",
                        isAdmin = viewModel.user?.isAdmin == true,
                        onUnavailableDaysClick = { router?.navigate("/unavailable-days") },
                        onPreferencesClick = { router?.navigate("/preferences") },
                        onLogoutClick = { viewModel.logout() }
                    )

                main {
                    style("margin", "50px 100px")
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

    /* To retain state during hot reload, this function can return anything (e.g. state serialized to json)
    * And it will be passed to start in state parameter */
    override fun dispose(): String? = null
}
