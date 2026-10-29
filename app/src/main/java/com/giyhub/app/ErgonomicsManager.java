package com.giyhub.app;

public class ErgonomicsManager {

    private static final int EYE_STRAIN_INTERVAL_MINUTES = 20;

    public static boolean shouldPromptEyeBreak(int continuousMinutesUsed) {
        if (continuousMinutesUsed <= 0) return false;
        return (continuousMinutesUsed % EYE_STRAIN_INTERVAL_MINUTES == 0);
    }

    public static String getEyeBreakMessage() {
        return "20-20-20 Eye Break: Look at something 20 feet away for 20 seconds to protect your eyes!";
    }
}
