package com.giyhub.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.Locale;

public class DeviceHardwareProfiler {

    public enum DeviceTier {
        LOW_END("Budget / Entry (RAM < 4GB)", "Qwen2.5-0.5B-Instruct-Q4_K_M.gguf"),
        MID_RANGE("Balanced / Mid-Range (RAM 4GB-6GB)", "SmolLM2-1.7B-Instruct-Q4_K_M.gguf"),
        HIGH_END("Performance / Flagship (RAM 8GB+)", "Llama-3.2-3B-Instruct-Q4_K_M.gguf");

        public final String description;
        public final String recommendedModel;

        DeviceTier(String description, String recommendedModel) {
            this.description = description;
            this.recommendedModel = recommendedModel;
        }
    }

    public static class HardwareProfile {
        public long totalRamBytes;
        public long availableRamBytes;
        public long freeStorageBytes;
        public int cpuCores;
        public DeviceTier tier;

        public HardwareProfile(long totalRamBytes, long availableRamBytes, long freeStorageBytes, int cpuCores, DeviceTier tier) {
            this.totalRamBytes = totalRamBytes;
            this.availableRamBytes = availableRamBytes;
            this.freeStorageBytes = freeStorageBytes;
            this.cpuCores = cpuCores;
            this.tier = tier;
        }

        public double getTotalRamGb() {
            return totalRamBytes / (1024.0 * 1024.0 * 1024.0);
        }

        public double getFreeStorageGb() {
            return freeStorageBytes / (1024.0 * 1024.0 * 1024.0);
        }

        public String getSummary() {
            return String.format(Locale.US,
                    "RAM: %.1f GB | Free Disk: %.1f GB | CPU Cores: %d\nTier: %s\nRecommended AI: %s",
                    getTotalRamGb(), getFreeStorageGb(), cpuCores, tier.description, tier.recommendedModel);
        }
    }

    public static HardwareProfile scanDevice(Context context) {
        long totalRam = 0;
        long availRam = 0;

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (actManager != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            totalRam = memInfo.totalMem;
            availRam = memInfo.availMem;
        }

        long freeStorage = 0;
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            freeStorage = stat.getAvailableBytes();
        } catch (Exception ignored) {
            freeStorage = 2L * 1024 * 1024 * 1024; // 2GB fallback
        }

        int cores = Runtime.getRuntime().availableProcessors();
        DeviceTier tier = evaluateTier(totalRam);

        return new HardwareProfile(totalRam, availRam, freeStorage, cores, tier);
    }

    public static DeviceTier evaluateTier(long totalRamBytes) {
        double ramGb = totalRamBytes / (1024.0 * 1024.0 * 1024.0);
        if (ramGb < 3.8) {
            return DeviceTier.LOW_END;
        } else if (ramGb < 7.2) {
            return DeviceTier.MID_RANGE;
        } else {
            return DeviceTier.HIGH_END;
        }
    }
}
