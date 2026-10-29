package com.giyhub.app;

import java.util.Locale;

public final class ScreenTimeHelper {

    private ScreenTimeHelper() {}

    public static String formatDuration(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format(Locale.US, "%dh %02dm", hours, minutes);
    }

    public static int calculateProgress(int totalMinutes, int limitMinutes) {
        if (limitMinutes <= 0) {
            return 0;
        }
        int percent = (int) (((float) totalMinutes / limitMinutes) * 100);
        return Math.max(0, Math.min(100, percent));
    }

    public static int calculateRemaining(int totalMinutes, int limitMinutes) {
        return Math.max(0, limitMinutes - totalMinutes);
    }
}
