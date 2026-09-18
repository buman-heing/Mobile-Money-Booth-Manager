package com.moneybooth.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.Magenta
import com.moneybooth.app.ui.transactions.TransactionListScreen

private enum class HomeTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    DASHBOARD("Home", Icons.Outlined.Home, Icons.Rounded.Home),
    TRANSACTIONS("Activity", Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong),
    SHIFT("Shift", Icons.Outlined.Schedule, Icons.Rounded.Schedule),
    DEV_TOOLS("Parser", Icons.Outlined.Code, Icons.Rounded.Code),
    MORE("More", Icons.Outlined.GridView, Icons.Rounded.GridView),
}

private enum class MoreSection { MENU, BOOTHS, EMPLOYEES, SETTINGS }

@Composable
fun HomeScreen(navController: NavHostController, container: AppContainer, business: BusinessEntity, onPinReset: () -> Unit) {
    var selectedTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    var moreSection by remember { mutableStateOf(MoreSection.MENU) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                HomeTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            selectedTab = tab
                            if (tab == HomeTab.MORE) moreSection = MoreSection.MENU
                        },
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
                HomeTab.DASHBOARD -> DashboardScreen(
                    container = container,
                    businessName = business.name,
                    onTransactionClick = { id -> navController.navigate(Destinations.transactionDetail(id)) },
                    onSeeAllTransactions = { selectedTab = HomeTab.TRANSACTIONS },
                )
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("More", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column {
                MoreMenuItem("Booths", "Locations and float accounts", Icons.Rounded.Storefront, MaterialTheme.colorScheme.primary) {
                    onSelect(MoreSection.BOOTHS)
                }
                Divider()
                MoreMenuItem("Employees", "Staff, roles and shifts", Icons.Rounded.People, MoneyIn) {
                    onSelect(MoreSection.EMPLOYEES)
                }
                Divider()
                MoreMenuItem("Settings", "PIN, SMS permissions, device", Icons.Rounded.Settings, Magenta) {
                    onSelect(MoreSection.SETTINGS)
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier.fillMaxWidth().padding(start = 72.dp).height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun MoreMenuItem(title: String, subtitle: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(tint.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
