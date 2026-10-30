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

    @Test
    public void testModelCompatibilityReport() {
        long ram = 3L * 1024 * 1024 * 1024; // 3GB RAM (Deficient for 3B model requiring 6GB)
        long avail = 1L * 1024 * 1024 * 1024;
        long storage = 10L * 1024 * 1024 * 1024; // 10GB storage
        DeviceHardwareProfiler.HardwareProfile profile = new DeviceHardwareProfiler.HardwareProfile(
                ram, avail, storage, 8, DeviceHardwareProfiler.DeviceTier.LOW_END
        );

        ModelCatalog.ModelItem heavyModel = new ModelCatalog.ModelItem(
                "llama3-3b", "Llama 3.2 3B", 2000, 6.0, "https://hf.co/model", "llama3.gguf", "Heavy 3B model"
        );

        DeviceHardwareProfiler.CompatibilityReport report = DeviceHardwareProfiler.evaluateModelCompatibility(profile, heavyModel);
        assertNotNull(report);
        assertTrue(report.isRamWarning);
        assertTrue(report.statusTitle.contains("RAM"));

        // Test compatible model
        ModelCatalog.ModelItem tinyModel = new ModelCatalog.ModelItem(
                "qwen-0.5b", "Qwen 2.5 0.5B", 390, 1.5, "https://hf.co/model", "qwen.gguf", "Lightweight"
        );

        DeviceHardwareProfiler.CompatibilityReport tinyReport = DeviceHardwareProfiler.evaluateModelCompatibility(profile, tinyModel);
        assertNotNull(tinyReport);
        assertTrue(tinyReport.isCompatible);
        assertTrue(tinyReport.statusTitle.contains("Compatible"));
    }
}
