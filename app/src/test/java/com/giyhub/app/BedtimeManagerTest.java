package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BedtimeManagerTest {

    @Test
    public void testOvernightBedtime() {
        // Bedtime 21:00 to 07:00
        assertTrue(BedtimeManager.isHourWithinBedtime(21, 21, 7)); // start boundary
        assertTrue(BedtimeManager.isHourWithinBedtime(23, 21, 7)); // late night
        assertTrue(BedtimeManager.isHourWithinBedtime(2, 21, 7));  // early morning
        assertTrue(BedtimeManager.isHourWithinBedtime(6, 21, 7));  // before wake
        assertFalse(BedtimeManager.isHourWithinBedtime(7, 21, 7)); // end boundary (wake up)
        assertFalse(BedtimeManager.isHourWithinBedtime(14, 21, 7)); // midday
    }

    @Test
    public void testDaytimeSchedule() {
        // Focus window 13:00 to 15:00
        assertTrue(BedtimeManager.isHourWithinBedtime(13, 13, 15));
        assertTrue(BedtimeManager.isHourWithinBedtime(14, 13, 15));
        assertFalse(BedtimeManager.isHourWithinBedtime(12, 13, 15));
        assertFalse(BedtimeManager.isHourWithinBedtime(15, 13, 15));
    }
}
