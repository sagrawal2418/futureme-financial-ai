package com.futureme.financialai.util

import kotlin.math.abs
import kotlin.math.roundToInt

fun money(value: Double): String {
    val rounded = abs(value).roundToInt()
    val grouped = rounded.toString().reversed().chunked(3).joinToString(",").reversed()
    return (if (value < 0.0) "-$" else "$") + grouped
}

fun compactMoney(value: Double): String = when {
    abs(value) >= 1_000_000.0 -> {
        val prefix = if (value < 0.0) "-$" else "$"
        prefix + oneDecimal(abs(value) / 1_000_000.0) + "M"
    }
    abs(value) >= 1_000.0 -> {
        val prefix = if (value < 0.0) "-$" else "$"
        prefix + (abs(value) / 1_000.0).roundToInt() + "K"
    }
    else -> money(value)
}

fun signedMoney(value: Double): String =
    (if (value >= 0.0) "+" else "-") + money(abs(value))

fun oneDecimal(value: Double): String =
    ((value * 10.0).roundToInt() / 10.0).toString()
