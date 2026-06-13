package com.example.chaisecrorepati.utils

fun formatRupees(amount: Double): String {
    return when {
        amount >= 1_00_00_000 -> "₹%.2f Cr".format(amount / 1_00_00_000)
        amount >= 1_00_000    -> "₹%.2f L".format(amount / 1_00_000)
        amount >= 1_000       -> "₹%.1f K".format(amount / 1_000)
        else                  -> "₹%.0f".format(amount)
    }
}

fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}