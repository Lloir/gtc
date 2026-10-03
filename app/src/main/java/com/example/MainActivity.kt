package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExchangeMarketItemsScreen
import com.example.ui.screens.FleetLogisticsScreen
import com.example.ui.screens.IndustryCalculatorScreen
import com.example.ui.screens.TradeAlertsScreen
import com.example.ui.screens.WebGameScreen
import com.example.ui.theme.CyanElectric
import com.example.ui.theme.GalacticTycoonsTheme
import com.example.ui.theme.LossRed
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalacticTycoonsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Market : Screen("market", "Market", Icons.Default.CurrencyExchange)
    object Fleet : Screen("fleet", "Fleet", Icons.Default.LocalShipping)
    object Game : Screen("game", "Game Client", Icons.Default.Public)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.NotificationsActive)
    object Calculator : Screen("calculator", "Calculator", Icons.Default.CurrencyExchange)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalacticTycoonsTheme {
                GalacticTycoonsAppNavigation()
            }
        }
    }
}

@Composable
fun GalacticTycoonsAppNavigation(
    viewModel: GalacticTycoonsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val navigationItems = listOf(
        Screen.Dashboard,
        Screen.Market,
        Screen.Fleet,
        Screen.Game,
        Screen.Alerts
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = SpaceCardBg,
                contentColor = CyanElectric,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (screen == Screen.Alerts && unreadNotifs > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = LossRed,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotifs", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanElectric,
                            selectedTextColor = CyanElectric,
                            indicatorColor = Color(0x3300E5FF),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMarket = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFleet = {
                        navController.navigate(Screen.Fleet.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAlerts = {
                        navController.navigate(Screen.Alerts.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGame = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCalculator = {
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Market.route) {
                ExchangeMarketItemsScreen(
                    viewModel = viewModel,
                    onNavigateToAlerts = {
                        navController.navigate(Screen.Alerts.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Fleet.route) {
                FleetLogisticsScreen(viewModel = viewModel)
            }

            composable(Screen.Game.route) {
                WebGameScreen(viewModel = viewModel)
            }

            composable(Screen.Alerts.route) {
                TradeAlertsScreen(viewModel = viewModel)
            }

            composable(Screen.Calculator.route) {
                IndustryCalculatorScreen(viewModel = viewModel)
            }
        }
    }
}
