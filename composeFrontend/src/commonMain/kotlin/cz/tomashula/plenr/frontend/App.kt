package cz.tomashula.plenr.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.tomashula.plenr.frontend.screen.arrangetrainings.ArrangeTrainingsScreen
import cz.tomashula.plenr.frontend.screen.arrangetrainings.ArrangeTrainingsScreenViewModel
import cz.tomashula.plenr.frontend.screen.home.AdminHomeScreen
import cz.tomashula.plenr.frontend.screen.login.LoginScreen
import cz.tomashula.plenr.frontend.screen.forgotpassword.ForgotPasswordScreen
import cz.tomashula.plenr.frontend.screen.home.AdminHomeScreenViewModel
import cz.tomashula.plenr.frontend.screen.manageusers.ManageUsersScreen
import cz.tomashula.plenr.frontend.screen.manageusers.ManageUsersScreenViewModel
import cz.tomashula.plenr.frontend.ui.component.AppHeader
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
)
{
    val appViewModel = viewModel { AppViewModel() }
    val navController = rememberNavController()

    var currentDestination by remember { mutableStateOf<String?>("") }
    var isInitialized by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appViewModel.init()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination.route
        }
        isInitialized = true
        isLoggedIn = appViewModel.isLoggedIn
    }

    if (!isInitialized)
        return

    MaterialTheme {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LaunchedEffect(Unit) {
                onNavHostReady(navController)
            }
            AppHeader(
                title = currentDestination ?: "Unknown destination",
                user = appViewModel.user,
                onLogoClick = {
                    if (navController.currentDestination?.route != PlenrScreen.Home.name)
                        navController.navigate(PlenrScreen.Home.name) {
                            popUpTo(PlenrScreen.Home.name) { inclusive = true }
                        }
                },
                onLogoutClick = {
                    appViewModel.logout()
                    isLoggedIn = false
                    navController.navigate(PlenrScreen.Login.name) {
                        popUpTo(PlenrScreen.Login.name) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) PlenrScreen.Home.name else PlenrScreen.Login.name,
            ) {
                composable(PlenrScreen.Login.name) {
                    LoginScreen(
                        appViewModel = appViewModel,
                        onLogin = { navController.navigate(PlenrScreen.Home.name) },
                        onForgotPassword = { navController.navigate(PlenrScreen.ForgotPassword.name) },
                    )
                }

                composable(PlenrScreen.ForgotPassword.name) {
                    ForgotPasswordScreen(
                        appViewModel = appViewModel,
                        onBackToLogin = { navController.navigate(PlenrScreen.Login.name) }
                    )
                }

                composable(PlenrScreen.Home.name) {
                    if (appViewModel.user!!.isAdmin)
                        AdminHomeScreen(
                            viewModel = viewModel { AdminHomeScreenViewModel(appViewModel) },
                            onManageUsersClick = { navController.navigate(PlenrScreen.ManageUsers.name) },
                            onArrangeTrainingsClick = { navController.navigate(PlenrScreen.ArrangeTrainings.name) },
                        )
                    else
                        TODO("Not yet implemented for non-admin users")
                }
                
                composable(PlenrScreen.ManageUsers.name) {
                    ManageUsersScreen(
                        viewModel = viewModel { ManageUsersScreenViewModel(appViewModel) },
                    )
                }
                
                composable(PlenrScreen.ArrangeTrainings.name) {
                    ArrangeTrainingsScreen(
                        viewModel = viewModel { ArrangeTrainingsScreenViewModel(appViewModel) },
                    )
                }
            }
        }
    }
}
