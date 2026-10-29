package com.giyhub.app;

import java.util.Calendar;

public class BedtimeManager {

    private int startHour = 21; // 9:00 PM
    private int endHour = 7;    // 7:00 AM

    public BedtimeManager() {}

    public BedtimeManager(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public boolean isBedtimeActiveNow() {
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        return isHourWithinBedtime(currentHour, startHour, endHour);
    }

    public static boolean isHourWithinBedtime(int currentHour, int start, int end) {
        if (start > end) {
            // Overnight window, e.g. 21:00 to 07:00
            return currentHour >= start || currentHour < end;
        } else if (start < end) {
            // Daytime window, e.g. 13:00 to 15:00
            return currentHour >= start && currentHour < end;
        } else {
            // Equal start and end means disabled
            return false;
        }
    }

    public String getFormattedSchedule() {
        return String.format("%02d:00 – %02d:00", startHour, endHour);
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }
}
