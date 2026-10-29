package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.SecureRandom;
import java.util.UUID;

public class PairingManager {

    private static final String PREF_NAME = "andro_assist_pairing";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_FAMILY_ID = "family_id";
    private static final String KEY_PAIRING_CODE = "pairing_code";
    private static final String KEY_PAIRED = "is_paired";

    private final SharedPreferences prefs;

    public PairingManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureDeviceId();
    }

    private void ensureDeviceId() {
        if (!prefs.contains(KEY_DEVICE_ID)) {
            String newId = "DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply();
        }
    }

    public String getDeviceId() {
        return prefs.getString(KEY_DEVICE_ID, "DEV-UNKNOWN");
    }

    public String generatePairingCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String codeStr = "PAIR-" + code;
        prefs.edit().putString(KEY_PAIRING_CODE, codeStr).apply();
        return codeStr;
    }

    public String getActivePairingCode() {
        return prefs.getString(KEY_PAIRING_CODE, generatePairingCode());
    }

    public boolean pairWithCode(String enteredCode, String targetFamilyId) {
        if (enteredCode != null && enteredCode.trim().length() >= 6) {
            prefs.edit()
                    .putString(KEY_FAMILY_ID, targetFamilyId)
                    .putBoolean(KEY_PAIRED, true)
                    .apply();
            return true;
        }
        return false;
    }

    public boolean isPaired() {
        return prefs.getBoolean(KEY_PAIRED, false);
    }

    public String getFamilyId() {
        return prefs.getString(KEY_FAMILY_ID, "FAM-" + getDeviceId().substring(4));
    }

    public void unpair() {
        prefs.edit().remove(KEY_FAMILY_ID).putBoolean(KEY_PAIRED, false).apply();
    }
}
