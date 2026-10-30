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

    public static class CompatibilityReport {
        public final boolean isCompatible;
        public final boolean isStorageWarning;
        public final boolean isRamWarning;
        public final String statusTitle;
        public final String message;
        public final String recommendation;

        public CompatibilityReport(boolean isCompatible, boolean isStorageWarning, boolean isRamWarning, String statusTitle, String message, String recommendation) {
            this.isCompatible = isCompatible;
            this.isStorageWarning = isStorageWarning;
            this.isRamWarning = isRamWarning;
            this.statusTitle = statusTitle;
            this.message = message;
            this.recommendation = recommendation;
        }
    }

    public static CompatibilityReport evaluateModelCompatibility(HardwareProfile profile, ModelCatalog.ModelItem model) {
        if (profile == null || model == null) {
            return new CompatibilityReport(false, false, false, "Unknown Profile", "Unable to analyze device.", "Run scan first.");
        }

        double ramGb = profile.getTotalRamGb();
        double freeStorageMb = profile.freeStorageBytes / (1024.0 * 1024.0);

        boolean ramDeficient = ramGb < model.minRamGb;
        boolean storageDeficient = freeStorageMb < (model.sizeMb * 1.3); // 30% safety margin

        if (storageDeficient) {
            return new CompatibilityReport(
                    false,
                    true,
                    ramDeficient,
                    "⚠️ Insufficient Storage",
                    String.format(Locale.US, "Model requires %d MB. Device has only %.0f MB free disk space.", model.sizeMb, freeStorageMb),
                    "Free up storage or use Cloud AI (Pollinations / Puter.js)."
            );
        }

        if (ramDeficient) {
            return new CompatibilityReport(
                    false,
                    false,
                    true,
                    "⚠️ RAM Pressure Warning",
                    String.format(Locale.US, "%s requires %.1f GB RAM, but device has %.1f GB RAM. Loading it locally may cause lag or OOM.", model.name, model.minRamGb, ramGb),
                    "Recommended alternative: " + profile.tier.recommendedModel + " or Cloud AI routing."
            );
        }

        return new CompatibilityReport(
                true,
                false,
                false,
                "✅ Hardware Fully Compatible",
                String.format(Locale.US, "Device verified: %.1f GB RAM, %.1f GB free storage, %d CPU cores. Fast local inference supported.",
                        ramGb, profile.getFreeStorageGb(), profile.cpuCores),
                "Ready for offline Agentic AI reasoning."
        );
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
