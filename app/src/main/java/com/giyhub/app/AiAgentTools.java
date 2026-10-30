package com.giyhub.app;

import android.content.Context;
import java.util.Locale;

public class AiAgentTools {

    public static class PendingAction {
        public String toolName;
        public String description;
        public Runnable executeRunnable;

        public PendingAction(String toolName, String description, Runnable executeRunnable) {
            this.toolName = toolName;
            this.description = description;
            this.executeRunnable = executeRunnable;
        }
    }

    public static String getSystemToolsPrompt() {
        return "You are Andro-Assist Agent, an intelligent guardian and device assistant.\n" +
                "You have access to the following system tools:\n" +
                "1. [TOOL:check_screen_time()] - Get current daily usage and remaining minutes.\n" +
                "2. [TOOL:set_daily_quota(minutes)] - Adjust daily screen time limit.\n" +
                "3. [TOOL:toggle_instant_pause(true/false)] - Pause or resume all child devices.\n" +
                "4. [TOOL:toggle_sentinel(true/false)] - Enable/disable 18+ web content shield.\n" +
                "5. [TOOL:check_bedtime()] - Check sleep hours and downtime status.\n" +
                "6. [TOOL:award_bonus(minutes)] - Award bonus screen time for chores.\n" +
                "7. [TOOL:get_device_health()] - Check app self-healing diagnostics.\n" +
                "To execute an action, output the tool tag in your response like: [TOOL:action_name(param)].";
    }

    public static String executeTool(Context context, String toolTag, RoleManager roleManager, SentinelManager sentinelManager, RewardManager rewardManager, BedtimeManager bedtimeManager, AppHealthManager healthManager) {
        if (toolTag == null) return "No tool invoked.";

        try {
            if (toolTag.contains("check_screen_time")) {
                int limit = roleManager.getDailyLimitMinutes();
                return String.format(Locale.US, "Current configured daily quota is %d minutes (%s).", limit, ScreenTimeHelper.formatDuration(limit));
            } else if (toolTag.contains("set_daily_quota")) {
                int minutes = extractIntArg(toolTag, 120);
                roleManager.setDailyLimitMinutes(minutes);
                return String.format(Locale.US, "Daily screen time quota updated to %d minutes.", minutes);
            } else if (toolTag.contains("toggle_instant_pause")) {
                boolean active = toolTag.contains("true");
                roleManager.setInstantPause(active);
                return active ? "Instant Pause ACTIVATED. Child devices are paused." : "Instant Pause DEACTIVATED. Child devices resumed.";
            } else if (toolTag.contains("toggle_sentinel")) {
                boolean active = toolTag.contains("true");
                sentinelManager.setFilterEnabled(active);
                return active ? "Sentinel 18+ Web Shield is now ENABLED." : "Sentinel 18+ Web Shield is now DISABLED.";
            } else if (toolTag.contains("check_bedtime")) {
                boolean active = bedtimeManager.isBedtimeActiveNow();
                return String.format(Locale.US, "Bedtime window is %s. Currently active: %s", bedtimeManager.getFormattedSchedule(), active ? "YES" : "NO");
            } else if (toolTag.contains("award_bonus")) {
                int bonus = extractIntArg(toolTag, 15);
                rewardManager.addBonusMinutes(bonus);
                return String.format(Locale.US, "Awarded +%d minutes bonus screen time.", bonus);
            } else if (toolTag.contains("get_device_health")) {
                return healthManager.getHealthDiagnostics();
            }
        } catch (Exception e) {
            return "Tool execution error: " + e.getMessage();
        }

        return "Unknown tool tag: " + toolTag;
    }

    public static PendingAction parsePendingAction(Context context, String toolTag, RoleManager roleManager, SentinelManager sentinelManager, RewardManager rewardManager, BedtimeManager bedtimeManager, AppHealthManager healthManager) {
        if (toolTag == null || !toolTag.startsWith("[TOOL:")) return null;

        if (toolTag.contains("set_daily_quota")) {
            int mins = extractIntArg(toolTag, 120);
            return new PendingAction("Set Quota", "Change daily limit to " + mins + " minutes", () -> roleManager.setDailyLimitMinutes(mins));
        } else if (toolTag.contains("toggle_instant_pause")) {
            boolean active = toolTag.contains("true");
            return new PendingAction("Instant Pause", (active ? "Pause" : "Resume") + " all child devices", () -> roleManager.setInstantPause(active));
        } else if (toolTag.contains("toggle_sentinel")) {
            boolean active = toolTag.contains("true");
            return new PendingAction("Sentinel Shield", (active ? "Enable" : "Disable") + " 18+ Web Filter", () -> sentinelManager.setFilterEnabled(active));
        } else if (toolTag.contains("award_bonus")) {
            int bonus = extractIntArg(toolTag, 15);
            return new PendingAction("Award Bonus", "Grant +" + bonus + "m bonus screen time", () -> rewardManager.addBonusMinutes(bonus));
        }
        return null;
    }

    private static int extractIntArg(String toolTag, int fallback) {
        try {
            int start = toolTag.indexOf('(');
            int end = toolTag.indexOf(')');
            if (start != -1 && end > start) {
                String sub = toolTag.substring(start + 1, end).trim();
                return Integer.parseInt(sub.replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {}
        return fallback;
    }
}
