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
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.MoneyOut

/** Renders a minor-units amount as a signed currency string, e.g. "+ ZMW 48.00". */
@Composable
fun AmountText(
    amountMinor: Long?,
    modifier: Modifier = Modifier,
    currency: String = Money.DEFAULT_CURRENCY,
    direction: TransactionDirection? = null,
    style: TextStyle = LocalTextStyle.current,
    unknownLabel: String = "Not supplied",
    signed: Boolean = true,
) {
    val color = when (direction) {
        TransactionDirection.IN -> MoneyIn
        TransactionDirection.OUT -> MoneyOut
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when {
        !signed || amountMinor == null -> ""
        direction == TransactionDirection.IN -> "+ "
        direction == TransactionDirection.OUT -> "− "
        else -> ""
    }
    val text = if (amountMinor == null) unknownLabel else prefix + Money.formatWithCurrency(amountMinor, currency)
    Text(text = text, modifier = modifier, style = style.copy(fontWeight = FontWeight.Bold, color = color))
}
