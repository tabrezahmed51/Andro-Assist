package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

public class RoleManager {

    private static final String PREF_NAME = "andro_assist_prefs";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_PARENT_PIN = "parent_pin";
    private static final String KEY_DAILY_LIMIT = "daily_limit_minutes";
    private static final String KEY_INSTANT_PAUSE = "instant_pause_active";

    public static final String ROLE_UNSET = "UNSET";
    public static final String ROLE_PARENT = "PARENT";
    public static final String ROLE_CHILD = "CHILD";

    private static final int DEFAULT_LIMIT_MINUTES = 120; // 2 hours
    private static final String DEFAULT_PIN = "1234";

    private final SharedPreferences prefs;

    public RoleManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_UNSET);
    }

    public void setRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public boolean isParent() {
        return ROLE_PARENT.equals(getRole());
    }

    public boolean isChild() {
        return ROLE_CHILD.equals(getRole());
    }

    public boolean verifyPin(String inputPin) {
        String savedPin = prefs.getString(KEY_PARENT_PIN, DEFAULT_PIN);
        return savedPin.equals(inputPin);
    }

    public void setPin(String newPin) {
        if (newPin != null && newPin.length() >= 4) {
            prefs.edit().putString(KEY_PARENT_PIN, newPin).apply();
        }
    }

    public int getDailyLimitMinutes() {
        return prefs.getInt(KEY_DAILY_LIMIT, DEFAULT_LIMIT_MINUTES);
    }

    public void setDailyLimitMinutes(int minutes) {
        int clamped = Math.max(15, Math.min(1440, minutes)); // between 15m and 24h
        prefs.edit().putInt(KEY_DAILY_LIMIT, clamped).apply();
    }

    public boolean isInstantPauseActive() {
        return prefs.getBoolean(KEY_INSTANT_PAUSE, false);
    }

    public void setInstantPause(boolean active) {
        prefs.edit().putBoolean(KEY_INSTANT_PAUSE, active).apply();
    }

    public void resetRole() {
        prefs.edit().remove(KEY_ROLE).apply();
    }
}
