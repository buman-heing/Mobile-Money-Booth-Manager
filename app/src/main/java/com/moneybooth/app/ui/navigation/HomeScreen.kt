package com.moneybooth.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import com.moneybooth.app.ui.booths.BoothListScreen
import com.moneybooth.app.ui.dashboard.DashboardScreen
import com.moneybooth.app.ui.devtools.DevSmsParserScreen
import com.moneybooth.app.ui.employees.EmployeeListScreen
import com.moneybooth.app.ui.settings.SettingsScreen
import com.moneybooth.app.ui.shifts.ShiftScreen
import com.moneybooth.app.ui.transactions.TransactionListScreen

private enum class HomeTab(val label: String) {
    DASHBOARD("Dashboard"),
    TRANSACTIONS("Transactions"),
    SHIFT("Shift"),
    DEV_TOOLS("Dev Tools"),
    MORE("More"),
}

private enum class MoreSection { MENU, BOOTHS, EMPLOYEES, SETTINGS }

@Composable
fun HomeScreen(navController: NavHostController, container: AppContainer, business: BusinessEntity, onPinReset: () -> Unit) {
    var selectedTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    var moreSection by remember { mutableStateOf(MoreSection.MENU) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.DASHBOARD,
                    onClick = { selectedTab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(HomeTab.DASHBOARD.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.TRANSACTIONS,
                    onClick = { selectedTab = HomeTab.TRANSACTIONS },
                    icon = { Icon(Icons.Filled.Receipt, contentDescription = null) },
                    label = { Text(HomeTab.TRANSACTIONS.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.SHIFT,
                    onClick = { selectedTab = HomeTab.SHIFT },
                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    label = { Text(HomeTab.SHIFT.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.DEV_TOOLS,
                    onClick = { selectedTab = HomeTab.DEV_TOOLS },
                    icon = { Icon(Icons.Filled.BugReport, contentDescription = null) },
                    label = { Text(HomeTab.DEV_TOOLS.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.MORE,
                    onClick = { selectedTab = HomeTab.MORE; moreSection = MoreSection.MENU },
                    icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
                    label = { Text(HomeTab.MORE.label) },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                HomeTab.DASHBOARD -> DashboardScreen(container = container)
                HomeTab.TRANSACTIONS -> TransactionListScreen(
                    container = container,
                    onTransactionClick = { id -> navController.navigate(Destinations.transactionDetail(id)) },
                    onAddTransaction = { navController.navigate(Destinations.TRANSACTION_ADD) },
                    onReviewClick = { navController.navigate(Destinations.REVIEW) },
                )
                HomeTab.SHIFT -> ShiftScreen(businessId = business.id, container = container)
                HomeTab.DEV_TOOLS -> DevSmsParserScreen(container = container)
                HomeTab.MORE -> when (moreSection) {
                    MoreSection.MENU -> MoreMenu(onSelect = { moreSection = it })
                    MoreSection.BOOTHS -> BoothListScreen(
                        businessId = business.id,
                        container = container,
                        onAddBooth = { navController.navigate(Destinations.BOOTH_ADD) },
                    )
                    MoreSection.EMPLOYEES -> EmployeeListScreen(
                        businessId = business.id,
                        container = container,
                        onAddEmployee = { navController.navigate(Destinations.EMPLOYEE_ADD) },
                    )
                    MoreSection.SETTINGS -> SettingsScreen(
                        businessId = business.id,
                        container = container,
                        onResetPin = onPinReset,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreMenu(onSelect: (MoreSection) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        MoreMenuItem("Booths", Icons.Filled.Storefront) { onSelect(MoreSection.BOOTHS) }
        MoreMenuItem("Employees", Icons.Filled.People) { onSelect(MoreSection.EMPLOYEES) }
        MoreMenuItem("Settings", Icons.Filled.Settings) { onSelect(MoreSection.SETTINGS) }
    }
}

@Composable
private fun MoreMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp))
        }
    }
}
