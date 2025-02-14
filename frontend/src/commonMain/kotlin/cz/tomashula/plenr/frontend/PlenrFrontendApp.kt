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
            var router by remember { mutableStateOf<Router?>(null) }

            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.init()
                initialized = true
            }

            if (initialized)
            {
                val initPath = if (viewModel.isLoggedIn)
                    Route.HOME
                else
                    Route.LOGIN

                if (viewModel.isLoggedIn)
                    plenrHeader(
                        title = router?.currentPath?.path?.title() ?: "",
                        isAdmin = viewModel.user?.isAdmin == true,
                        onUnavailableDaysClick = { router?.navigate(Route.UNAVAILABLE_DAYS) },
                        onPreferencesClick = { router?.navigate(Route.PREFERENCES) },
                        onLogoutClick = { viewModel.logout() }
                    )

                main {
                    style("margin", "50px 100px")
                    BrowserRouter(initPath) {
                        router = Router.current
                        LaunchedEffect(Unit) {
                            if (!viewModel.adminExists())
                                router!!.navigate(Route.ADMIN_SETUP)
                        }
                        route(Route.HOME) {
                            if (viewModel.user?.isAdmin == true)
                                adminHomePage(viewModel)
                            else
                                userHomePage(viewModel)
                        }
                        route(Route.ADMIN_SETUP) {
                            adminSetupPage(viewModel)
                        }
                        route(Route.SET_PASSWORD) {
                            string { token ->
                                passwordSetupPage(viewModel, token)
                            }
                        }
                        route(Route.LOGIN) {
                            loginPage(viewModel)
                        }
                        route(Route.PREFERENCES) {
                            userPreferencesPage(viewModel)
                        }
                        if (viewModel.user?.isAdmin == true)
                        {
                            route(Route.MANAGE_USERS) {
                                manageUsersPage(viewModel)
                            }
                            route(Route.ARRANGE_TRAININGS) {
                                arrangeTrainingsPage(viewModel)
                            }
                            route(Route.ADD_USER) {
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

    private fun String.title() = when(this)
    {
        // CONSIDER: Declaring each route's title along with the route
        Route.HOME -> "Home"
        Route.ADMIN_SETUP -> "Admin Setup"
        Route.SET_PASSWORD -> "Set Password"
        Route.LOGIN -> "Login"
        Route.UNAVAILABLE_DAYS -> "Unavailable Days"
        Route.PREFERENCES -> "Preferences"
        Route.ARRANGE_TRAININGS -> "Arrange Trainings"
        Route.MANAGE_USERS -> "Manage Users"
        Route.ADD_USER -> "Add User"
        else -> ""
    }

    /* To retain state during hot reload, this function can return anything (e.g. state serialized to json)
    * And it will be passed to start in state parameter */
    override fun dispose(): String? = null
}
