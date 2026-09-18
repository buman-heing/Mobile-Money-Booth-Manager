package com.moneybooth.app.sms.airtel.matchers

/**
 * Regex fragment for an Airtel amount ("900.00", "1,030.00", "5").
 * Deliberately excludes a trailing period so "Bal ZMW 51.36.Com" captures "51.36", not "51.36.".
 */
internal const val AMOUNT = """(\d[\d,]*(?:\.\d+)?)"""
