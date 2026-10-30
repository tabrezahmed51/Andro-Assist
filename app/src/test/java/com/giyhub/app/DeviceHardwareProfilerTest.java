package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeviceHardwareProfilerTest {

    @Test
    public void testTierEvaluation() {
        // 2 GB RAM -> Low End
        long twoGb = 2L * 1024 * 1024 * 1024;
        assertEquals(DeviceHardwareProfiler.DeviceTier.LOW_END, DeviceHardwareProfiler.evaluateTier(twoGb));

        // 6 GB RAM -> Mid Range
        long sixGb = 6L * 1024 * 1024 * 1024;
        assertEquals(DeviceHardwareProfiler.DeviceTier.MID_RANGE, DeviceHardwareProfiler.evaluateTier(sixGb));

        // 12 GB RAM -> High End
        long twelveGb = 12L * 1024 * 1024 * 1024;
        assertEquals(DeviceHardwareProfiler.DeviceTier.HIGH_END, DeviceHardwareProfiler.evaluateTier(twelveGb));
    }

    @Test
    public void testHardwareProfileSummary() {
        long ram = 4L * 1024 * 1024 * 1024;
        long avail = 2L * 1024 * 1024 * 1024;
        long storage = 32L * 1024 * 1024 * 1024;
        DeviceHardwareProfiler.HardwareProfile profile = new DeviceHardwareProfiler.HardwareProfile(
                ram, avail, storage, 8, DeviceHardwareProfiler.DeviceTier.MID_RANGE
        );

        String summary = profile.getSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("RAM: 4.0 GB"));
        assertTrue(summary.contains("CPU Cores: 8"));
        assertTrue(summary.contains("SmolLM2-1.7B-Instruct-Q4_K_M.gguf"));
    }
}
