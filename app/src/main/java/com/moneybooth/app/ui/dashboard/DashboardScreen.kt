package com.moneybooth.app.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.theme.MoneyInGreen
import com.moneybooth.app.ui.theme.MoneyOutRed

@Composable
fun DashboardScreen(container: AppContainer) {
    val viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory(container))
    val count by viewModel.observeTodayCount(null).collectAsStateWithLifecycle(initialValue = 0)
    val moneyIn by viewModel.observeTodayMoneyIn(null).collectAsStateWithLifecycle(initialValue = 0L)
    val moneyOut by viewModel.observeTodayMoneyOut(null).collectAsStateWithLifecycle(initialValue = 0L)
    val balance by viewModel.observeMobileMoneyBalance(null).collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("Today", style = MaterialTheme.typography.headlineMedium)

        DashboardMetricCard("Transactions", count.toString())
        DashboardMetricCard("Money In", Money.formatWithCurrency(moneyIn), MoneyInGreen)
        DashboardMetricCard("Money Out", Money.formatWithCurrency(moneyOut), MoneyOutRed)
        DashboardMetricCard(
            "Mobile Money Balance",
            balance?.let { Money.formatWithCurrency(it) } ?: "Not supplied",
        )
    }
}

@Composable
private fun DashboardMetricCard(label: String, value: String, valueColor: Color? = null) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
