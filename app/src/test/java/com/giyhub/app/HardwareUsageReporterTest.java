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

    @Test
    public void testCategoryDistribution() {
        java.util.List<HardwareUsageReporter.AppMetricItem> apps = new java.util.ArrayList<>();
        apps.add(new HardwareUsageReporter.AppMetricItem(
                "org.khancademy.kids", "Khan Academy Kids", 60 * 60 * 1000L, "01h 00m 00s", 50, 80, 50.0
        ));
        apps.add(new HardwareUsageReporter.AppMetricItem(
                "com.google.android.youtube", "YouTube", 60 * 60 * 1000L, "01h 00m 00s", 70, 120, 50.0
        ));

        java.util.Map<String, Float> distribution = HardwareUsageReporter.getCategoryDistribution(apps);
        org.junit.Assert.assertNotNull(distribution);
        org.junit.Assert.assertTrue(distribution.containsKey("Education"));
        org.junit.Assert.assertTrue(distribution.containsKey("Entertainment"));
        org.junit.Assert.assertEquals(60f, distribution.get("Education"), 0.1f);
        org.junit.Assert.assertEquals(60f, distribution.get("Entertainment"), 0.1f);
    }
}
