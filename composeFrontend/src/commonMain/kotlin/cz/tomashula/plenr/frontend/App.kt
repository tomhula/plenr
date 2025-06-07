package cz.tomashula.plenr.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.tomashula.plenr.frontend.screen.HomeScreen
import cz.tomashula.plenr.frontend.screen.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    val appViewModel = viewModel { AppViewModel() }
    val navController = rememberNavController()
    
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        appViewModel.init()
        isInitialized = true
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
            
            NavHost(
                navController = navController,
                startDestination = PlenrScreen.Login.name,
            ) {
                composable(PlenrScreen.Login.name) {
                    LoginScreen(
                        appViewModel = appViewModel,
                        onLogin = { navController.navigate(PlenrScreen.Home.name) },
                        onForgotPassword = { navController.navigate(PlenrScreen.ForgotPassword.name) },
                    )
                }
                
                composable(PlenrScreen.Home.name) {
                    HomeScreen(
                        appViewModel = appViewModel,
                    )
                }
            }
        }
    }
}
