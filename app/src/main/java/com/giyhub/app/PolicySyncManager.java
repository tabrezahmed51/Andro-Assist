package com.giyhub.app;

import org.json.JSONException;
import org.json.JSONObject;

public class PolicySyncManager {

    public static class DevicePolicy {
        public String familyId;
        public String deviceId;
        public int dailyLimitMinutes;
        public boolean instantPauseActive;
        public int bedtimeStartHour;
        public int bedtimeEndHour;
        public int bonusMinutes;
        public long syncTimestamp;

        public DevicePolicy(String familyId, String deviceId, int limit, boolean pause, int bedStart, int bedEnd, int bonus) {
            this.familyId = familyId;
            this.deviceId = deviceId;
            this.dailyLimitMinutes = limit;
            this.instantPauseActive = pause;
            this.bedtimeStartHour = bedStart;
            this.bedtimeEndHour = bedEnd;
            this.bonusMinutes = bonus;
            this.syncTimestamp = System.currentTimeMillis();
        }
    }

    public static String serializePolicy(DevicePolicy policy) {
        try {
            JSONObject json = new JSONObject();
            json.put("familyId", policy.familyId);
            json.put("deviceId", policy.deviceId);
            json.put("dailyLimitMinutes", policy.dailyLimitMinutes);
            json.put("instantPauseActive", policy.instantPauseActive);
            json.put("bedtimeStartHour", policy.bedtimeStartHour);
            json.put("bedtimeEndHour", policy.bedtimeEndHour);
            json.put("bonusMinutes", policy.bonusMinutes);
            json.put("syncTimestamp", policy.syncTimestamp);
            return json.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static DevicePolicy deserializePolicy(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            String familyId = json.optString("familyId", "FAM-DEFAULT");
            String deviceId = json.optString("deviceId", "DEV-DEFAULT");
            int limit = json.optInt("dailyLimitMinutes", 120);
            boolean pause = json.optBoolean("instantPauseActive", false);
            int bedStart = json.optInt("bedtimeStartHour", 21);
            int bedEnd = json.optInt("bedtimeEndHour", 7);
            int bonus = json.optInt("bonusMinutes", 0);
            return new DevicePolicy(familyId, deviceId, limit, pause, bedStart, bedEnd, bonus);
        } catch (JSONException e) {
            return new DevicePolicy("FAM-DEFAULT", "DEV-DEFAULT", 120, false, 21, 7, 0);
        }
    }

    public static void applyPolicyToDevice(RoleManager roleManager, DevicePolicy policy) {
        if (roleManager != null && policy != null) {
            roleManager.setDailyLimitMinutes(policy.dailyLimitMinutes);
            roleManager.setInstantPause(policy.instantPauseActive);
        }
    }
}
