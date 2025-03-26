package com.example.qaautomation.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.qaautomation.ui.screens.browser.BrowserScreen
import com.example.qaautomation.ui.screens.ip.IpGeolocationScreen
import com.example.qaautomation.ui.screens.network.NetworkLogsScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Browser : Screen("browser")
    object NetworkLogs : Screen("network_logs")
    object IpGeolocation : Screen("ip_geolocation")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onBrowserClick = { navController.navigate(Screen.Browser.route) },
                onNetworkClick = { navController.navigate(Screen.NetworkLogs.route) },
                onIpGeolocationClick = { navController.navigate(Screen.IpGeolocation.route) }
            )
        }
        
        composable(Screen.Browser.route) {
            BrowserScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.NetworkLogs.route) {
            NetworkLogsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.IpGeolocation.route) {
            IpGeolocationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 