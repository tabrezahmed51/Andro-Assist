package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HardwareUsageReporterTest {

    @Test
    public void testFormatHms() {
        assertEquals("00h 00m 00s", HardwareUsageReporter.formatHms(0));

        // 45 seconds
        assertEquals("00h 00m 45s", HardwareUsageReporter.formatHms(45 * 1000L));

        // 2 minutes, 15 seconds
        assertEquals("00h 02m 15s", HardwareUsageReporter.formatHms((2 * 60 + 15) * 1000L));

        // 3 hours, 25 minutes, 40 seconds
        long millis = (3 * 3600 + 25 * 60 + 40) * 1000L;
        assertEquals("03h 25m 40s", HardwareUsageReporter.formatHms(millis));
    }
}
