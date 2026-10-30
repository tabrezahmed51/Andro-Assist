package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentMemoryManager {

    private static final String PREF_NAME = "andro_assist_agent_memory";
    private static final String KEY_CHAT_THREAD = "persisted_chat_thread";

    public static final int MAX_USER_TOKENS = 100;
    public static final int MAX_THREAD_TOKENS = 3000;

    private final SharedPreferences prefs;

    public AgentMemoryManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Estimates token count using standard BPE heuristic:
     * Combines word splits, punctuation splits, and character length averaging.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String trimmed = text.trim();
        String[] words = trimmed.split("\\s+");
        int punctuationCount = trimmed.replaceAll("[a-zA-Z0-9\\s]", "").length();
        int charBased = (trimmed.length() + 3) / 4;
        int wordBased = words.length + (punctuationCount / 2);

        return Math.max(charBased, wordBased);
    }

    /**
     * Checks if the user's prompt respects the 100-token maximum limit.
     */
    public static boolean isUserPromptWithinLimit(String prompt) {
        return estimateTokens(prompt) <= MAX_USER_TOKENS;
    }

    /**
     * Truncates a user prompt to stay strictly within 100 tokens.
     */
    public static String truncateToMaxUserTokens(String prompt) {
        if (isUserPromptWithinLimit(prompt)) {
            return prompt;
        }

        String[] words = prompt.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            String candidate = sb.length() == 0 ? word : sb + " " + word;
            if (estimateTokens(candidate) > MAX_USER_TOKENS) {
                break;
            }
            sb.append(sb.length() == 0 ? word : " " + word);
        }
        return sb.toString();
    }

    /**
     * Builds a sliding window conversation history up to 3,000 tokens.
     * Preserves chronological order while dropping oldest messages if budget is exceeded.
     */
    public static List<ChatMessage> buildBoundedThread(List<ChatMessage> fullHistory) {
        if (fullHistory == null || fullHistory.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatMessage> bounded = new ArrayList<>();
        int accumulatedTokens = 0;

        // Iterate backwards from most recent message
        for (int i = fullHistory.size() - 1; i >= 0; i--) {
            ChatMessage msg = fullHistory.get(i);
            int msgTokens = estimateTokens(msg.text);
            if (msg.thinkingProcess != null && !msg.thinkingProcess.isEmpty()) {
                msgTokens += estimateTokens(msg.thinkingProcess);
            }

            if (accumulatedTokens + msgTokens > MAX_THREAD_TOKENS) {
                break; // Stop taking older messages
            }

            accumulatedTokens += msgTokens;
            bounded.add(msg);
        }

        // Restore chronological order (earliest to latest)
        Collections.reverse(bounded);
        return bounded;
    }

    /**
     * Formats bounded messages into a multi-turn transcript for LLM context.
     */
    public static String formatConversationTranscript(List<ChatMessage> boundedHistory) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : boundedHistory) {
            if (msg.isUser) {
                sb.append("User: ").append(msg.text).append("\n");
            } else {
                sb.append("Agent: ").append(msg.text).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Persists chat thread to local SharedPreferences JSON.
     */
    public void saveChatThread(List<ChatMessage> messages) {
        try {
            JSONArray array = new JSONArray();
            List<ChatMessage> bounded = buildBoundedThread(messages);

            for (ChatMessage msg : bounded) {
                JSONObject obj = new JSONObject();
                obj.put("text", msg.text);
                obj.put("isUser", msg.isUser);
                obj.put("thinkingProcess", msg.thinkingProcess != null ? msg.thinkingProcess : "");
                obj.put("timestamp", msg.timestamp);
                obj.put("toolResult", msg.toolResult != null ? msg.toolResult : "");
                array.put(obj);
            }

            prefs.edit().putString(KEY_CHAT_THREAD, array.toString()).apply();
        } catch (Exception ignored) {}
    }

    /**
     * Restores persisted chat thread from storage.
     */
    public List<ChatMessage> loadChatThread() {
        List<ChatMessage> list = new ArrayList<>();
        String json = prefs.getString(KEY_CHAT_THREAD, null);
        if (json == null || json.isEmpty()) {
            return list;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ChatMessage msg = new ChatMessage(
                        obj.optString("text", ""),
                        obj.optBoolean("isUser", false),
                        obj.optString("thinkingProcess", ""),
                        null
                );
                msg.timestamp = obj.optLong("timestamp", System.currentTimeMillis());
                msg.toolResult = obj.optString("toolResult", null);
                list.add(msg);
            }
        } catch (Exception ignored) {}

        return list;
    }

    /**
     * Clears persisted memory and starts fresh.
     */
    public void clearMemory() {
        prefs.edit().remove(KEY_CHAT_THREAD).apply();
    }
}
