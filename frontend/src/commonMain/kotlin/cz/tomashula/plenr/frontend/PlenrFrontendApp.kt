package cz.tomashula.plenr.frontend

import androidx.compose.runtime.*
import app.softwork.routingcompose.RouteBuilder
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.frontend.component.plenrHeader
import cz.tomashula.plenr.frontend.page.admin.addUserPage
import cz.tomashula.plenr.frontend.page.admin.adminHomePage
import cz.tomashula.plenr.frontend.page.admin.arrangeTrainingsPage
import cz.tomashula.plenr.frontend.page.admin.manageUsersPage
import cz.tomashula.plenr.frontend.page.adminsetup.adminSetupPage
import cz.tomashula.plenr.frontend.page.login.forgotPasswordPage
import cz.tomashula.plenr.frontend.page.login.loginPage
import cz.tomashula.plenr.frontend.page.passwordsetup.passwordSetupPage
import cz.tomashula.plenr.frontend.page.user.availabilityPage
import cz.tomashula.plenr.frontend.page.user.userPreferencesPage
import cz.tomashula.plenr.frontend.page.userHomePage
import dev.kilua.Application
import dev.kilua.compose.root
import dev.kilua.html.Color
import dev.kilua.html.h1t
import dev.kilua.html.main
import dev.kilua.progress.Progress
import dev.kilua.progress.ProgressOptions
import dev.kilua.routing.SimpleBrowserRouter
import dev.kilua.useModule
import kotlinx.coroutines.launch

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
            var loading by remember { mutableStateOf(true) }
            val progress = remember { Progress(ProgressOptions(color = Color("#2e98d1"))) }

            LaunchedEffect(Unit) {
                viewModel.init()
                loading = false
            }

            LaunchedEffect(loading) {
                if (loading)
                    progress.start()
                else
                    progress.end()
            }

            plenrHeader(
                title = router?.currentPath()?.path?.title() ?: "",
                user = viewModel.user,
                onLogoClick = { router?.navigate(Route.HOME) },
                onAvailabilityClick = { router?.navigate(Route.AVAILABILITY) },
                onPreferencesClick = { router?.navigate(Route.PREFERENCES) },
                onLogoutClick = { viewModel.logout(); router?.navigate(Route.LOGIN) }
            )

            if (loading)
                return@root
            
            @Composable
            fun RouteBuilder.authOnlyRoute(route: String, block: @Composable () -> Unit)
            {
                route(route) {
                    if (viewModel.isLoggedIn)
                        block()
                    else
                        // Directly navigation causes issues (the url changes, but the login content is not loaded)
                        rememberCoroutineScope().launch { router?.navigate(Route.LOGIN) }
                }
            }

            main {
                style("margin", "24px 100px")
                SimpleBrowserRouter("/") {
                    if (loading) 
                        return@SimpleBrowserRouter
                    
                    router = Router.current

                    LaunchedEffect(Unit) {
                        if (!viewModel.adminExists())
                            router!!.navigate(Route.ADMIN_SETUP)
                    }
                    authOnlyRoute(Route.HOME) {
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
                    route(Route.FORGOT_PASSWORD) {
                        forgotPasswordPage(viewModel)
                    }
                    authOnlyRoute(Route.PREFERENCES) {
                        userPreferencesPage(viewModel)
                    }
                    authOnlyRoute(Route.AVAILABILITY) {
                        availabilityPage(viewModel)
                    }
                    if (viewModel.user?.isAdmin == true)
                    {
                        authOnlyRoute(Route.MANAGE_USERS) {
                            manageUsersPage(viewModel)
                        }
                        authOnlyRoute(Route.ARRANGE_TRAININGS) {
                            arrangeTrainingsPage(viewModel)
                        }
                        authOnlyRoute(Route.ADD_USER) {
                            addUserPage(viewModel)
                        }
                    }
                    noMatch {
                        h1t(text = "Page not found")
                    }
                }
            }
        }
    }

    private fun String.title() = when (this)
    {
        // CONSIDER: Declaring each route's title along with the route
        Route.HOME -> "Home"
        Route.ADMIN_SETUP -> "Admin Setup"
        Route.SET_PASSWORD -> "Set Password"
        Route.LOGIN -> "Login"
        Route.AVAILABILITY -> "Availability"
        Route.PREFERENCES -> "Preferences"
        Route.ARRANGE_TRAININGS -> "Arrange Trainings"
        Route.MANAGE_USERS -> "Manage Users"
        Route.ADD_USER -> "Add User"
        Route.FORGOT_PASSWORD -> "Forgot password"
        else -> ""
    }

    /* To retain state during hot reload, this function can return anything (e.g. state serialized to json)
    * And it will be passed to start in state parameter */
    override fun dispose(): String? = null
}
