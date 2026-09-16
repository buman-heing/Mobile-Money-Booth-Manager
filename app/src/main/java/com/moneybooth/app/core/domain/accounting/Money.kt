package com.moneybooth.app.core.domain.accounting

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * All monetary amounts are stored and summed as integer minor units (e.g. ngwee for ZMW,
 * 1 ZMW = 100 minor units) rather than Double, so reconciliation sums never accumulate
 * binary floating-point error and can never produce a phantom discrepancy.
 */
object Money {
    const val DEFAULT_CURRENCY = "ZMW"
    private const val MINOR_UNITS_PER_MAJOR = 100L

    /** Parses a decimal string like "48.00" or "0.06" into minor units. Returns null if unparseable. */
    fun parseToMinorUnits(text: String): Long? {
        val cleaned = text.trim().replace(",", "")
        val decimal = cleaned.toBigDecimalOrNull() ?: return null
        return decimal.setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .toLong()
    }

    fun formatMajor(minorUnits: Long): String {
        val major = BigDecimal(minorUnits).movePointLeft(2)
        return major.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    fun formatWithCurrency(minorUnits: Long, currency: String = DEFAULT_CURRENCY): String =
        "$currency ${formatMajor(minorUnits)}"

    private fun String.toBigDecimalOrNull(): BigDecimal? = try {
        BigDecimal(this)
    } catch (e: NumberFormatException) {
        null
    }
}
