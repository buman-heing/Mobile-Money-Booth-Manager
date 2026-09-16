package com.moneybooth.app.ui.reconciliation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
fun ReconciliationSummaryScreen(shiftId: Long, container: AppContainer, onDone: () -> Unit) {
    val viewModel: ReconciliationViewModel = viewModel(factory = AppViewModelFactory(container))
    val reconciliation by viewModel.observeByShift(shiftId).collectAsStateWithLifecycle(initialValue = null)
    val current = reconciliation

    if (current == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Finalizing reconciliation...")
        }
        return
    }

    val differenceColor = when {
        current.cashDifferenceMinor == 0L -> MoneyInGreen
        else -> MoneyOutRed
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Shift reconciliation", style = MaterialTheme.typography.headlineMedium)

        SummaryCard("Opening cash", Money.formatWithCurrency(current.openingCashMinor))
        SummaryCard("Cash in", "+ ${Money.formatWithCurrency(current.cashInMinor)}", MoneyInGreen)
        SummaryCard("Cash out", "− ${Money.formatWithCurrency(current.cashOutMinor)}", MoneyOutRed)
        SummaryCard("Expected cash", Money.formatWithCurrency(current.expectedCashMinor))
        SummaryCard("Actual cash counted", Money.formatWithCurrency(current.actualCashMinor))
        SummaryCard("Difference", Money.formatWithCurrency(current.cashDifferenceMinor), differenceColor)

        if (current.expectedMobileMoneyBalanceMinor != null) {
            Text(
                "Mobile money (informational)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
            SummaryCard("Expected balance", Money.formatWithCurrency(current.expectedMobileMoneyBalanceMinor))
            SummaryCard(
                "Provider-reported balance",
                current.latestProviderReportedBalanceMinor?.let { Money.formatWithCurrency(it) } ?: "Not supplied",
            )
        }

        if (current.unclassifiedTransactionCount > 0) {
            Text(
                "${current.unclassifiedTransactionCount} transaction(s) this shift are still unclassified.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("Done")
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, valueColor: Color? = null) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
