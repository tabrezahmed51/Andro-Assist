package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SentinelManager {

    private static final String PREF_NAME = "andro_assist_sentinel";
    private static final String KEY_FILTER_ENABLED = "sentinel_filter_enabled";

    // Known restricted domain indicators & category markers
    private static final Set<String> BLOCKED_KEYWORD_PATTERNS = new HashSet<>(Arrays.asList(
            "porn", "xxx", "adult", "sex", "nsfw", "casino", "gambling", "bet365", "betting"
    ));

    public static final String CLOUDFLARE_FAMILY_DNS_IPV4 = "1.1.1.3";
    public static final String CLEANBROWSING_FAMILY_DNS_IPV4 = "185.228.168.168";

    private final SharedPreferences prefs;

    public SentinelManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFilterEnabled() {
        // Defaults to true (Protection Active)
        return prefs.getBoolean(KEY_FILTER_ENABLED, true);
    }

    public void setFilterEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_FILTER_ENABLED, enabled).apply();
    }

    public boolean isDomainRestricted(String urlOrDomain) {
        if (!isFilterEnabled() || urlOrDomain == null) {
            return false;
        }

        String lower = urlOrDomain.toLowerCase(Locale.US).trim();

        for (String keyword : BLOCKED_KEYWORD_PATTERNS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public String appendSafeSearchParameters(String queryUrl) {
        if (!isFilterEnabled() || queryUrl == null) {
            return queryUrl;
        }

        if (queryUrl.contains("google.") && !queryUrl.contains("safe=active")) {
            return queryUrl + (queryUrl.contains("?") ? "&safe=active" : "?safe=active");
        }
        if (queryUrl.contains("bing.") && !queryUrl.contains("adlt=strict")) {
            return queryUrl + (queryUrl.contains("?") ? "&adlt=strict" : "?adlt=strict");
        }
        if (queryUrl.contains("youtube.") && !queryUrl.contains("restrict=strict")) {
            return queryUrl + (queryUrl.contains("?") ? "&restrict=strict" : "?restrict=strict");
        }
        return queryUrl;
    }

    public String getActiveDnsProfile() {
        return isFilterEnabled() ? CLOUDFLARE_FAMILY_DNS_IPV4 : "Default System DNS";
    }
}
