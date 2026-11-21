package com.example.cheliuschess.model

data class TimeConfig(
    val minutes: Int = 10,
    val seconds: Int = 0,
    val incrementSeconds: Int = 0
) {
    fun getTotalMillis(): Long {
        return (minutes * 60L + seconds) * 1000L
    }

    fun getIncrementMillis(): Long {
        return incrementSeconds * 1000L
    }

    fun getFormattedPreset(): String {
        return if (incrementSeconds > 0) {
            "${minutes}m + ${incrementSeconds}s"
        } else {
            "${minutes}m ${seconds}s"
        }
    }
}
