package com.moneybooth.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.DeviceRole
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import com.moneybooth.app.ui.dashboard.BoothHomeScreen
import com.moneybooth.app.ui.dashboard.DashboardScreen
import com.moneybooth.app.ui.employees.EmployeeListScreen
import com.moneybooth.app.ui.settings.SettingsScreen
import com.moneybooth.app.ui.shifts.ShiftScreen
import com.moneybooth.app.ui.transactions.TransactionListScreen

private enum class Tab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Rounded.Home),
    ACTIVITY("Activity", Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong),
    SHIFT("Shift", Icons.Outlined.Schedule, Icons.Rounded.Schedule),
    TEAM("Team", Icons.Outlined.People, Icons.Rounded.People),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Rounded.Settings),
}

/** Employee phones run the day; owner phones run the business. Same app, different tabs. */
private fun tabsFor(role: DeviceRole): List<Tab> = when (role) {
    DeviceRole.OWNER -> listOf(Tab.HOME, Tab.ACTIVITY, Tab.TEAM, Tab.SETTINGS)
    else -> listOf(Tab.HOME, Tab.ACTIVITY, Tab.SHIFT, Tab.SETTINGS)
}

@Composable
fun HomeScreen(navController: NavHostController, container: AppContainer, business: BusinessEntity, onPinReset: () -> Unit) {
    val role by container.deviceSettingsStore.deviceRoleFlow.collectAsStateWithLifecycle()
    val tabs = tabsFor(role)
    var selectedTab by rememberSaveable { mutableStateOf(Tab.HOME) }
    if (selectedTab !in tabs) selectedTab = Tab.HOME

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                tabs.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                Tab.HOME -> if (role == DeviceRole.OWNER) {
                    DashboardScreen(
                        container = container,
                        businessName = business.name,
                        onTransactionClick = { id -> navController.navigate(Destinations.transactionDetail(id)) },
                        onSeeAllTransactions = { selectedTab = Tab.ACTIVITY },
                    )
                } else {
                    BoothHomeScreen(
                        container = container,
                        businessName = business.name,
                        onTransactionClick = { id -> navController.navigate(Destinations.transactionDetail(id)) },
                        onOpenShift = { selectedTab = Tab.SHIFT },
                        onOpenSettings = { selectedTab = Tab.SETTINGS },
                    )
                }
                Tab.ACTIVITY -> TransactionListScreen(
                    container = container,
                    onTransactionClick = { id -> navController.navigate(Destinations.transactionDetail(id)) },
                    onAddTransaction = { navController.navigate(Destinations.TRANSACTION_ADD) },
                    onReviewClick = { navController.navigate(Destinations.REVIEW) },
                )
                Tab.SHIFT -> ShiftScreen(businessId = business.id, container = container)
                Tab.TEAM -> EmployeeListScreen(
                    businessId = business.id,
                    container = container,
                    onAddEmployee = { navController.navigate(Destinations.EMPLOYEE_ADD) },
                )
                Tab.SETTINGS -> SettingsScreen(
                    businessId = business.id,
                    container = container,
                    onResetPin = onPinReset,
                    onOpenParser = { navController.navigate(Destinations.DEV_PARSER) },
                )
            }
        }
    }
}
