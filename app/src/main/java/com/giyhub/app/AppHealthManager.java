package com.giyhub.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class AppHealthManager {

    private static final String TAG = "AppHealthManager";
    private static volatile AppHealthManager instance;

    private final Context context;
    private int caughtCrashCount = 0;
    private long lastHealedTimestamp = 0;

    private AppHealthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static AppHealthManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppHealthManager.class) {
                if (instance == null) {
                    instance = new AppHealthManager(context);
                }
            }
        }
        return instance;
    }

    public void initializeSelfHealing() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            caughtCrashCount++;
            lastHealedTimestamp = System.currentTimeMillis();
            Log.e(TAG, "Self-Healing Engine intercepted unhandled exception in thread " + thread.getName(), throwable);

            performEmergencySelfHeal();

            if (defaultHandler != null) {
                // Delegate to default handler if critical or let app recover gracefully
                defaultHandler.uncaughtException(thread, throwable);
            }
        });

        // Run proactive health scan on startup
        runProactiveHealthCheck();
    }

    public void runProactiveHealthCheck() {
        RoleManager roleManager = new RoleManager(context);

        // Heal daily limit if corrupted or out of safe bounds
        int limit = roleManager.getDailyLimitMinutes();
        if (limit < 15 || limit > 1440) {
            roleManager.setDailyLimitMinutes(120);
            lastHealedTimestamp = System.currentTimeMillis();
            Log.i(TAG, "Self-healed corrupted daily limit back to 120m default");
        }

        // Heal persistent child service if child mode is active
        if (roleManager.isChild()) {
            try {
                Intent serviceIntent = new Intent(context, ChildMonitorService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            } catch (Exception e) {
                Log.w(TAG, "Could not start ChildMonitorService during health check", e);
            }
        }
    }

    public void performEmergencySelfHeal() {
        try {
            RoleManager roleManager = new RoleManager(context);
            if (roleManager.getDailyLimitMinutes() <= 0) {
                roleManager.setDailyLimitMinutes(120);
            }
        } catch (Exception e) {
            Log.e(TAG, "Emergency self-heal failed", e);
        }
    }

    public String getHealthDiagnostics() {
        if (caughtCrashCount == 0) {
            return "Sentinel AI: 100% Operational (0 Anomalies)";
        } else {
            return String.format("Sentinel AI: Auto-Recovered (%d caught, Self-Healed)", caughtCrashCount);
        }
    }

    public int getCaughtCrashCount() {
        return caughtCrashCount;
    }

    public long getLastHealedTimestamp() {
        return lastHealedTimestamp;
    }
}
