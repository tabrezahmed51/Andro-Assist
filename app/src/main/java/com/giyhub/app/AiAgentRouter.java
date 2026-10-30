package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

public class AiAgentRouter {

    private static final String PREF_NAME = "andro_assist_ai_router";
    private static final String KEY_ROUTER_MODE = "router_mode";
    private static final String KEY_AUTONOMY_MODE = "autonomy_mode";
    private static final String KEY_THINKING_MODE = "thinking_mode";
    private static final String KEY_SELECTED_MODEL = "selected_model_id";

    public enum RouterMode {
        AUTO("Auto Smart Fallback", "Routes internal device tasks locally and general tasks to cloud."),
        MANUAL("Manual Model Selection", "Directly uses the user-selected AI model without auto-switching.");

        public final String title;
        public final String description;

        RouterMode(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public enum AutonomyMode {
        YOLO("YOLO Mode (Fully Autonomous)", "Agent automatically executes tools and actions immediately."),
        ALWAYS_ASK("Always Ask (Confirmation)", "Agent asks parent for confirmation before running any tool."),
        MANUAL("Fully Manual (Prompt Only)", "Agent only gives recommendations. Actions must be done manually.");

        public final String title;
        public final String description;

        AutonomyMode(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public enum ThinkingMode {
        NO_THINKING("1. No Thinking", "Direct immediate response without reasoning trace."),
        DEFAULT("2. Default / Auto", "Adapts thinking depth automatically to query complexity."),
        LOW("3. Low Thinking", "Brief 1-step rationale before concluding."),
        MEDIUM("4. Medium Thinking", "Balanced multi-step reasoning and verification."),
        HIGH("5. High Thinking", "Deep analytical audit, safety evaluation, and comprehensive thought.");

        public final String title;
        public final String description;

        ThinkingMode(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public static class AgentResponse {
        public String thinkingProcess;
        public String responseText;
        public String toolExecuted;
        public AiAgentTools.PendingAction pendingAction;

        public AgentResponse(String thinkingProcess, String responseText, String toolExecuted, AiAgentTools.PendingAction pendingAction) {
            this.thinkingProcess = thinkingProcess;
            this.responseText = responseText;
            this.toolExecuted = toolExecuted;
            this.pendingAction = pendingAction;
        }
    }

    public interface RouterCallback {
        void onResponse(AgentResponse response);
        void onError(String error);
    }

    private final Context context;
    private final SharedPreferences prefs;
    private final ModelCatalog modelCatalog;
    private final AiCloudProviderManager cloudManager;
    private final RoleManager roleManager;
    private final SentinelManager sentinelManager;
    private final RewardManager rewardManager;
    private final BedtimeManager bedtimeManager;
    private final AppHealthManager healthManager;

    public AiAgentRouter(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.modelCatalog = new ModelCatalog(context);
        this.cloudManager = new AiCloudProviderManager(context);
        this.roleManager = new RoleManager(context);
        this.sentinelManager = new SentinelManager(context);
        this.rewardManager = new RewardManager(context);
        this.bedtimeManager = new BedtimeManager();
        this.healthManager = AppHealthManager.getInstance(context);
    }

    // --- Configurations ---
    public RouterMode getRouterMode() {
        String name = prefs.getString(KEY_ROUTER_MODE, RouterMode.AUTO.name());
        try {
            return RouterMode.valueOf(name);
        } catch (Exception e) {
            return RouterMode.AUTO;
        }
    }

    public void setRouterMode(RouterMode mode) {
        prefs.edit().putString(KEY_ROUTER_MODE, mode.name()).apply();
    }

    public AutonomyMode getAutonomyMode() {
        String name = prefs.getString(KEY_AUTONOMY_MODE, AutonomyMode.ALWAYS_ASK.name());
        try {
            return AutonomyMode.valueOf(name);
        } catch (Exception e) {
            return AutonomyMode.ALWAYS_ASK;
        }
    }

    public void setAutonomyMode(AutonomyMode mode) {
        prefs.edit().putString(KEY_AUTONOMY_MODE, mode.name()).apply();
    }

    public ThinkingMode getThinkingMode() {
        String name = prefs.getString(KEY_THINKING_MODE, ThinkingMode.DEFAULT.name());
        try {
            return ThinkingMode.valueOf(name);
        } catch (Exception e) {
            return ThinkingMode.DEFAULT;
        }
    }

    public void setThinkingMode(ThinkingMode mode) {
        prefs.edit().putString(KEY_THINKING_MODE, mode.name()).apply();
    }

    public String getSelectedModel() {
        return prefs.getString(KEY_SELECTED_MODEL, "qwen-0.5b");
    }

    public void setSelectedModel(String modelId) {
        prefs.edit().putString(KEY_SELECTED_MODEL, modelId).apply();
    }

    // --- Query Dispatching & Tool Execution ---
    public void processQuery(String userQuery, RouterCallback callback) {
        RouterMode routerMode = getRouterMode();
        ThinkingMode thinkingMode = getThinkingMode();
        AutonomyMode autonomyMode = getAutonomyMode();

        boolean isInternalTask = isInternalDeviceTask(userQuery);
        String loadedModel = modelCatalog.getLoadedModelId();

        // Build thinking instruction
        String thinkingPrompt = buildThinkingInstruction(thinkingMode);
        String systemPrompt = AiAgentTools.getSystemToolsPrompt() + "\n" + thinkingPrompt;

        // If AUTO mode & internal task with local model ready, or simple fast task:
        if (routerMode == RouterMode.AUTO && isInternalTask) {
            handleInternalDirectAgent(userQuery, thinkingMode, autonomyMode, callback);
            return;
        }

        // Otherwise use Cloud Providers (Pollinations / Puter) with fallback
        cloudManager.queryPollinations(userQuery, systemPrompt, new AiCloudProviderManager.CloudAiCallback() {
            @Override
            public void onSuccess(String responseText) {
                parseAndDeliverResponse(userQuery, responseText, thinkingMode, autonomyMode, callback);
            }

            @Override
            public void onError(String errorMessage) {
                // Fallback to local internal logic
                handleInternalDirectAgent(userQuery, thinkingMode, autonomyMode, callback);
            }
        });
    }

    private boolean isInternalDeviceTask(String query) {
        if (query == null) return false;
        String q = query.toLowerCase();
        return q.contains("screen time") || q.contains("quota") || q.contains("limit")
                || q.contains("pause") || q.contains("sentinel") || q.contains("18+")
                || q.contains("bedtime") || q.contains("sleep") || q.contains("chore")
                || q.contains("bonus") || q.contains("health") || q.contains("status");
    }

    private String buildThinkingInstruction(ThinkingMode mode) {
        switch (mode) {
            case NO_THINKING:
                return "Do not include any thinking tags or deliberation. Provide direct output immediately.";
            case LOW:
                return "Include a brief 1-sentence thought in <think>...</think> before concluding.";
            case MEDIUM:
                return "Include 2-3 structured reasoning steps in <think>...</think> before answering.";
            case HIGH:
                return "Perform comprehensive multi-step analysis, safety audit, and edge case check in <think>...</think> before answering.";
            case DEFAULT:
            default:
                return "If the question is complex, provide reasoning inside <think>...</think>, otherwise respond clearly.";
        }
    }

    private void handleInternalDirectAgent(String query, ThinkingMode thinkingMode, AutonomyMode autonomyMode, RouterCallback callback) {
        String q = query.toLowerCase();
        String toolTag = null;
        String directAnswer = "";
        String thought = "";

        if (q.contains("pause") && (q.contains("on") || q.contains("start") || q.contains("activate") || q.contains("dinner"))) {
            toolTag = "[TOOL:toggle_instant_pause(true)]";
            directAnswer = "Instant Pause activated. Child device screens are currently locked.";
            thought = "Parent requested dinner/instant pause. Activating system lock.";
        } else if (q.contains("pause") && (q.contains("off") || q.contains("stop") || q.contains("resume"))) {
            toolTag = "[TOOL:toggle_instant_pause(false)]";
            directAnswer = "Instant Pause deactivated. Child devices have resumed normal schedules.";
            thought = "Parent requested resuming activity. Deactivating system lock.";
        } else if (q.contains("screen time") || q.contains("usage") || q.contains("status")) {
            toolTag = "[TOOL:check_screen_time()]";
            int limit = roleManager.getDailyLimitMinutes();
            directAnswer = "Screen time limit is currently " + limit + " minutes (" + ScreenTimeHelper.formatDuration(limit) + ").";
            thought = "Querying local UsageStatsManager daily quota status.";
        } else if (q.contains("sentinel") || q.contains("18+") || q.contains("filter")) {
            boolean enable = !q.contains("off") && !q.contains("disable");
            toolTag = "[TOOL:toggle_sentinel(" + enable + ")]";
            directAnswer = enable ? "Sentinel 18+ Web Shield is active. Unsafe domains are blocked." : "Sentinel 18+ Web Shield disabled.";
            thought = "Adjusting Sentinel Web Guardian filter rule state.";
        } else if (q.contains("bedtime") || q.contains("sleep")) {
            toolTag = "[TOOL:check_bedtime()]";
            directAnswer = "Bedtime is configured for " + bedtimeManager.getFormattedSchedule() + ". Active now: " + (bedtimeManager.isBedtimeActiveNow() ? "YES" : "NO");
            thought = "Checking device clock against BedtimeManager schedule.";
        } else if (q.contains("bonus") || q.contains("chore")) {
            toolTag = "[TOOL:award_bonus(20)]";
            directAnswer = "Awarded +20 minutes bonus screen time for completed tasks.";
            thought = "Applying chore verification reward to child quota.";
        } else {
            directAnswer = "I am your Andro-Assist AI Agent. I can manage screen time quotas, instant pause, bedtime downtime, and Sentinel web filters.";
            thought = "General greeting / system capability summary.";
        }

        if (thinkingMode == ThinkingMode.NO_THINKING) {
            thought = "";
        }

        executeOrStageAction(query, directAnswer, thought, toolTag, autonomyMode, callback);
    }

    private void parseAndDeliverResponse(String userQuery, String rawResponse, ThinkingMode thinkingMode, AutonomyMode autonomyMode, RouterCallback callback) {
        String thought = "";
        String cleanResponse = rawResponse;

        if (rawResponse.contains("<think>") && rawResponse.contains("</think>")) {
            int start = rawResponse.indexOf("<think>") + 7;
            int end = rawResponse.indexOf("</think>");
            if (end > start) {
                thought = rawResponse.substring(start, end).trim();
                cleanResponse = (rawResponse.substring(0, start - 7) + rawResponse.substring(end + 8)).trim();
            }
        }

        if (thinkingMode == ThinkingMode.NO_THINKING) {
            thought = "";
        }

        // Look for tool tags in response
        String toolTag = null;
        if (cleanResponse.contains("[TOOL:")) {
            int start = cleanResponse.indexOf("[TOOL:");
            int end = cleanResponse.indexOf("]", start);
            if (end > start) {
                toolTag = cleanResponse.substring(start, end + 1);
                cleanResponse = cleanResponse.replace(toolTag, "").trim();
            }
        }

        executeOrStageAction(userQuery, cleanResponse, thought, toolTag, autonomyMode, callback);
    }

    private void executeOrStageAction(String query, String responseText, String thought, String toolTag, AutonomyMode autonomyMode, RouterCallback callback) {
        String toolResult = null;
        AiAgentTools.PendingAction pendingAction = null;

        if (toolTag != null) {
            if (autonomyMode == AutonomyMode.YOLO) {
                // Execute immediately
                toolResult = AiAgentTools.executeTool(context, toolTag, roleManager, sentinelManager, rewardManager, bedtimeManager, healthManager);
            } else if (autonomyMode == AutonomyMode.ALWAYS_ASK) {
                // Stage for confirmation
                pendingAction = AiAgentTools.parsePendingAction(context, toolTag, roleManager, sentinelManager, rewardManager, bedtimeManager, healthManager);
            }
            // In MANUAL mode, tool is ignored
        }

        callback.onResponse(new AgentResponse(thought, responseText, toolResult, pendingAction));
    }
}
