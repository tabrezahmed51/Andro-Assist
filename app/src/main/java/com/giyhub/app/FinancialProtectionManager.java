package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages financial protection policies, preventing surprise credit card bills,
 * accidental in-game microtransactions, and unauthorized app installations.
 */
public class FinancialProtectionManager {

    private static final String PREFS_NAME = "andro_assist_financial_guard";
    private static final String KEY_LEVEL = "financial_protection_level";
    private static final String KEY_BLOCKED_COUNT = "blocked_purchase_attempts_count";

    public enum ProtectionLevel {
        LEVEL_1_ALERT(
                "Level 1: Billing Watch",
                "Alerts parent instantly whenever payment gateways or Play Store purchase screens are accessed."
        ),
        LEVEL_2_RESTRICT(
                "Level 2: Install & Purchase Lock",
                "Disables sideloading/APK installations and mandates authentication for every single transaction."
        ),
        LEVEL_3_FORTRESS(
                "Level 3: Financial Fortress",
                "Total lockdown: Blocks Google Play billing, prevents app installs, and restricts financial/banking apps on child devices."
        );

        public final String title;
        public final String description;

        ProtectionLevel(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private static final Set<String> PACKAGE_INSTALLERS = new HashSet<>(Arrays.asList(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller"
    ));

    private static final Set<String> FINANCIAL_PAYMENT_PACKAGES = new HashSet<>(Arrays.asList(
            "com.google.android.apps.walletnfcrel",
            "com.paypal.android.p2pmobile",
            "com.venmo",
            "com.squareup.cash",
            "com.phonepe.app",
            "com.paytm.payments"
    ));

    private final SharedPreferences prefs;

    public FinancialProtectionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public ProtectionLevel getProtectionLevel() {
        String name = prefs.getString(KEY_LEVEL, ProtectionLevel.LEVEL_2_RESTRICT.name());
        try {
            return ProtectionLevel.valueOf(name);
        } catch (IllegalArgumentException e) {
            return ProtectionLevel.LEVEL_2_RESTRICT;
        }
    }

    public void setProtectionLevel(ProtectionLevel level) {
        if (level != null) {
            prefs.edit().putString(KEY_LEVEL, level.name()).apply();
        }
    }

    public boolean isInstallBlocked() {
        ProtectionLevel level = getProtectionLevel();
        return level == ProtectionLevel.LEVEL_2_RESTRICT || level == ProtectionLevel.LEVEL_3_FORTRESS;
    }

    public boolean isFinancialApp(String packageName) {
        return packageName != null && FINANCIAL_PAYMENT_PACKAGES.contains(packageName);
    }

    public boolean isPackageInstaller(String packageName) {
        return packageName != null && PACKAGE_INSTALLERS.contains(packageName);
    }

    /**
     * Determines whether an application package should be intercepted under current financial protection.
     */
    public boolean shouldInterceptPackage(String packageName) {
        if (packageName == null) return false;

        ProtectionLevel level = getProtectionLevel();

        if (level == ProtectionLevel.LEVEL_3_FORTRESS) {
            // Level 3 blocks both package installers and financial/payment gateways
            if (isPackageInstaller(packageName) || isFinancialApp(packageName)) {
                return true;
            }
        } else if (level == ProtectionLevel.LEVEL_2_RESTRICT) {
            // Level 2 blocks package installer dialogs
            if (isPackageInstaller(packageName)) {
                return true;
            }
        }

        return false;
    }

    public void recordBlockedAttempt(String packageName) {
        int current = getBlockedAttemptsCount();
        prefs.edit().putInt(KEY_BLOCKED_COUNT, current + 1).apply();
    }

    public int getBlockedAttemptsCount() {
        return prefs.getInt(KEY_BLOCKED_COUNT, 0);
    }

    public void resetBlockedAttemptsCount() {
        prefs.edit().putInt(KEY_BLOCKED_COUNT, 0).apply();
    }
}
