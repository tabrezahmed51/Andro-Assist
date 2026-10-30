package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiCloudProviderManager {

    private static final String PREF_NAME = "andro_assist_cloud_ai";
    private static final String KEY_POLLINATIONS_USER = "pollinations_user";
    private static final String KEY_POLLINATIONS_KEY = "pollinations_key";
    private static final String KEY_POLLINATIONS_ENABLED = "pollinations_enabled";

    private static final String KEY_PUTER_USER = "puter_user";
    private static final String KEY_PUTER_TOKEN = "puter_token";
    private static final String KEY_PUTER_ENABLED = "puter_enabled";

    public static final String POLLINATIONS_TEXT_URL = "https://text.pollinations.ai/";
    public static final String PUTER_AI_URL = "https://api.puter.com/drivers/call/puter-chat-completion";

    public interface CloudAiCallback {
        void onSuccess(String responseText);
        void onError(String errorMessage);
    }

    private final SharedPreferences prefs;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AiCloudProviderManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // --- Pollinations AI ---
    public boolean isPollinationsEnabled() {
        return prefs.getBoolean(KEY_POLLINATIONS_ENABLED, true);
    }

    public void setPollinationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_POLLINATIONS_ENABLED, enabled).apply();
    }

    public void savePollinationsAccount(String username, String apiKey) {
        prefs.edit()
                .putString(KEY_POLLINATIONS_USER, username)
                .putString(KEY_POLLINATIONS_KEY, apiKey)
                .putBoolean(KEY_POLLINATIONS_ENABLED, true)
                .apply();
    }

    public String getPollinationsUser() {
        return prefs.getString(KEY_POLLINATIONS_USER, "");
    }

    public String getPollinationsKey() {
        return prefs.getString(KEY_POLLINATIONS_KEY, "");
    }

    public void resetPollinations() {
        prefs.edit()
                .remove(KEY_POLLINATIONS_USER)
                .remove(KEY_POLLINATIONS_KEY)
                .putBoolean(KEY_POLLINATIONS_ENABLED, false)
                .apply();
    }

    // --- Puter.js / Puter AI ---
    public boolean isPuterEnabled() {
        return prefs.getBoolean(KEY_PUTER_ENABLED, false);
    }

    public void setPuterEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PUTER_ENABLED, enabled).apply();
    }

    public void savePuterAccount(String username, String token) {
        prefs.edit()
                .putString(KEY_PUTER_USER, username)
                .putString(KEY_PUTER_TOKEN, token)
                .putBoolean(KEY_PUTER_ENABLED, true)
                .apply();
    }

    public String getPuterUser() {
        return prefs.getString(KEY_PUTER_USER, "");
    }

    public String getPuterToken() {
        return prefs.getString(KEY_PUTER_TOKEN, "");
    }

    public void resetPuter() {
        prefs.edit()
                .remove(KEY_PUTER_USER)
                .remove(KEY_PUTER_TOKEN)
                .putBoolean(KEY_PUTER_ENABLED, false)
                .apply();
    }

    // --- Execution ---
    public void queryPollinations(String prompt, String systemPrompt, CloudAiCallback callback) {
        executor.execute(() -> {
            try {
                String encodedPrompt = URLEncoder.encode(prompt, "UTF-8");
                String fullUrl = POLLINATIONS_TEXT_URL + encodedPrompt;
                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    fullUrl += "?system=" + URLEncoder.encode(systemPrompt, "UTF-8");
                }

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                String key = getPollinationsKey();
                if (!key.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + key);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    String result = sb.toString().trim();
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    mainHandler.post(() -> callback.onError("Pollinations HTTP Error: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void queryPuter(String prompt, String systemPrompt, CloudAiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(PUTER_AI_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);

                String token = getPuterToken();
                if (!token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                JSONObject payload = new JSONObject();
                JSONArray messages = new JSONArray();

                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    JSONObject sysMsg = new JSONObject();
                    sysMsg.put("role", "system");
                    sysMsg.put("content", systemPrompt);
                    messages.put(sysMsg);
                }

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                messages.put(userMsg);

                payload.put("messages", messages);
                payload.put("model", "gpt-4o-mini");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    mainHandler.post(() -> callback.onSuccess(sb.toString().trim()));
                } else {
                    mainHandler.post(() -> callback.onError("Puter HTTP Error: " + code));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Puter connection error: " + e.getMessage()));
            }
        });
    }
}
