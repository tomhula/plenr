package cz.tomashula.plenr.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cz.tomashula.plenr.frontend.screen.adminsetup.AdminSetupScreen
import cz.tomashula.plenr.frontend.screen.arrangetrainings.ArrangeTrainingsScreen
import cz.tomashula.plenr.frontend.screen.arrangetrainings.ArrangeTrainingsScreenViewModel
import cz.tomashula.plenr.frontend.screen.home.AdminHomeScreen
import cz.tomashula.plenr.frontend.screen.login.LoginScreen
import cz.tomashula.plenr.frontend.screen.forgotpassword.ForgotPasswordScreen
import cz.tomashula.plenr.frontend.screen.home.AdminHomeScreenViewModel
import cz.tomashula.plenr.frontend.screen.setpassword.SetPasswordScreen
import cz.tomashula.plenr.frontend.screen.manageusers.ManageUsersScreen
import cz.tomashula.plenr.frontend.screen.manageusers.ManageUsersScreenViewModel
import cz.tomashula.plenr.frontend.ui.component.AppHeader
import cz.tomashula.plenr.frontend.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
)
{
    val appViewModel = viewModel { AppViewModel() }
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    var isInitialized by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var adminExists by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        appViewModel.init()
        isLoggedIn = appViewModel.isLoggedIn
        isInitialized = true
        adminExists = appViewModel.adminExists()
    }

    if (!isInitialized)
        return

    AppTheme(darkTheme = false) {
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
                /* TODO: This is a workaround, should have a proper translatable destination names */
                title = currentDestination?.route?.replace("cz.tomashula.plenr.frontend.PlenrScreen.", "") ?: "Unknown destination",
                user = appViewModel.user,
                onLogoClick = {
                    if (navController.currentDestination != PlenrScreen.Home)
                        navController.navigate(PlenrScreen.Home) {
                            popUpTo(PlenrScreen.Home) { inclusive = true }
                        }
                },
                onLogoutClick = {
                    appViewModel.logout()
                    isLoggedIn = false
                    navController.navigate(PlenrScreen.Login) {
                        popUpTo(PlenrScreen.Login) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(bottom = 16.dp)
            )

            NavHost(
                navController = navController,
                startDestination = when {
                    !adminExists -> PlenrScreen.AdminSetup
                    isLoggedIn -> PlenrScreen.Home
                    else -> PlenrScreen.Login
                },
            ) {
                composable<PlenrScreen.AdminSetup> {
                    AdminSetupScreen(
                        appViewModel = appViewModel,
                        onAdminSetupFinished = { 
                            adminExists = true
                            navController.navigate(PlenrScreen.Login) 
                        }
                    )
                }

                composable<PlenrScreen.Login> {
                    LoginScreen(
                        appViewModel = appViewModel,
                        onLogin = { navController.navigate(PlenrScreen.Home) },
                        onForgotPassword = { navController.navigate(PlenrScreen.ForgotPassword) },
                    )
                }

                composable<PlenrScreen.ForgotPassword> {
                    ForgotPasswordScreen(
                        appViewModel = appViewModel,
                        onBackToLogin = { navController.navigate(PlenrScreen.Login) }
                    )
                }

                composable<PlenrScreen.SetPassword> { backStackEntry ->
                    val token = backStackEntry.toRoute<PlenrScreen.SetPassword>().token
                    SetPasswordScreen(
                        appViewModel = appViewModel,
                        token = token,
                        onSuccess = { navController.navigate(PlenrScreen.Login) }
                    )
                }

                composable<PlenrScreen.Home> {
                    if (appViewModel.user?.isAdmin ?: false)
                        AdminHomeScreen(
                            viewModel = viewModel { AdminHomeScreenViewModel(appViewModel) },
                            onManageUsersClick = { navController.navigate(PlenrScreen.ManageUsers) },
                            onArrangeTrainingsClick = { navController.navigate(PlenrScreen.ArrangeTrainings) },
                        )
                    else
                        Text("User home screen not implemented yet.")
                }

                composable<PlenrScreen.ManageUsers> {
                    ManageUsersScreen(
                        viewModel = viewModel { ManageUsersScreenViewModel(appViewModel) },
                    )
                }

                composable<PlenrScreen.ArrangeTrainings> {
                    ArrangeTrainingsScreen(
                        viewModel = viewModel { ArrangeTrainingsScreenViewModel(appViewModel) },
                    )
                }
            }
        }
    }
}
