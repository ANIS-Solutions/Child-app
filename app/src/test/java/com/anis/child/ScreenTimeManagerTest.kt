package com.anis.child

import com.anis.child.data.ScreenTimeManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTimeManagerTest {

    @Test
    fun isTimeInRange_withinSameDay() {
        assertTrue("14:30 should be in range 09:00-17:00",
            ScreenTimeManager.isTimeInRange(14, 30, 9, 0, 17, 0))
    }

    @Test
    fun isTimeInRange_beforeStart() {
        assertFalse("08:00 should NOT be in range 09:00-17:00",
            ScreenTimeManager.isTimeInRange(8, 0, 9, 0, 17, 0))
    }

    @Test
    fun isTimeInRange_afterEnd() {
        assertFalse("18:00 should NOT be in range 09:00-17:00",
            ScreenTimeManager.isTimeInRange(18, 0, 9, 0, 17, 0))
    }

    @Test
    fun isTimeInRange_atBoundary() {
        assertTrue("09:00 should be in range 09:00-17:00",
            ScreenTimeManager.isTimeInRange(9, 0, 9, 0, 17, 0))
        assertTrue("17:00 should be in range 09:00-17:00",
            ScreenTimeManager.isTimeInRange(17, 0, 9, 0, 17, 0))
    }

    @Test
    fun isTimeInRange_overnight() {
        assertTrue("22:00 should be in range 21:00-07:00 (overnight)",
            ScreenTimeManager.isTimeInRange(22, 0, 21, 0, 7, 0))
        assertTrue("03:00 should be in range 21:00-07:00 (overnight)",
            ScreenTimeManager.isTimeInRange(3, 0, 21, 0, 7, 0))
        assertTrue("06:00 should be in range 21:00-07:00 (overnight)",
            ScreenTimeManager.isTimeInRange(6, 0, 21, 0, 7, 0))
        assertFalse("12:00 should NOT be in range 21:00-07:00 (overnight)",
            ScreenTimeManager.isTimeInRange(12, 0, 21, 0, 7, 0))
    }

    @Test
    fun isTimeInRange_overnightBoundary() {
        assertTrue("21:00 should be in range 21:00-07:00",
            ScreenTimeManager.isTimeInRange(21, 0, 21, 0, 7, 0))
        assertTrue("07:00 should be in range 21:00-07:00",
            ScreenTimeManager.isTimeInRange(7, 0, 21, 0, 7, 0))
    }

    @Test
    fun isTimeInRange_midnight() {
        assertTrue("00:00 should be in range 21:00-07:00",
            ScreenTimeManager.isTimeInRange(0, 0, 21, 0, 7, 0))
    }

    @Test
    fun isTimeInRange_fullDay() {
        assertTrue("12:00 should be in range 00:00-23:59",
            ScreenTimeManager.isTimeInRange(12, 0, 0, 0, 23, 59))
        assertTrue("00:00 should be in range 00:00-23:59",
            ScreenTimeManager.isTimeInRange(0, 0, 0, 0, 23, 59))
        assertTrue("23:59 should be in range 00:00-23:59",
            ScreenTimeManager.isTimeInRange(23, 59, 0, 0, 23, 59))
    }
}
