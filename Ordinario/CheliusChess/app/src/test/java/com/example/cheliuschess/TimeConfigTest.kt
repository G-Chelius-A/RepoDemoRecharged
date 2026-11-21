package com.example.cheliuschess

import com.example.cheliuschess.model.TimeConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeConfigTest {

    @Test
    fun testTimeConfig_millisAndFormat() {
        val config = TimeConfig(minutes = 5, seconds = 30, incrementSeconds = 3)
        assertEquals(330000L, config.getTotalMillis())
        assertEquals(3000L, config.getIncrementMillis())
        assertEquals("5m + 3s", config.getFormattedPreset())
    }
}
