package com.moneybooth.app.ui.common

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.TransactionDirection

/** Renders a minor-units amount as a currency string, e.g. "ZMW 48.00". */
@Composable
fun AmountText(
    amountMinor: Long?,
    modifier: Modifier = Modifier,
    currency: String = Money.DEFAULT_CURRENCY,
    direction: TransactionDirection? = null,
    style: TextStyle = LocalTextStyle.current,
    unknownLabel: String = "Not supplied",
) {
    val color = when (direction) {
        TransactionDirection.IN -> MoneyInGreenColor()
        TransactionDirection.OUT -> MoneyOutRedColor()
        else -> MaterialTheme.colorScheme.onSurface
    }
    val text = if (amountMinor == null) unknownLabel else Money.formatWithCurrency(amountMinor, currency)
    Text(text = text, modifier = modifier, style = style.copy(fontWeight = FontWeight.SemiBold, color = color))
}

@Composable
private fun MoneyInGreenColor() = com.moneybooth.app.ui.theme.MoneyInGreen

@Composable
private fun MoneyOutRedColor() = com.moneybooth.app.ui.theme.MoneyOutRed
