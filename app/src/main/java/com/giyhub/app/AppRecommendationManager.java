package com.giyhub.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides curated educational & creative apps for children,
 * and scans installed apps for high-risk attributes (loot boxes, gambling, unmoderated chat).
 */
public class AppRecommendationManager {

    public enum RiskLevel {
        SAFE("Safe & Recommended", 0xFF10B981),
        CAUTION("Caution: In-App Purchases", 0xFFF59E0B),
        HIGH_RISK("High Risk: Gambling / Chat Trap", 0xFFEF4444);

        public final String label;
        public final int colorHex;

        RiskLevel(String label, int colorHex) {
            this.label = label;
            this.colorHex = colorHex;
        }
    }

    public static class RecommendedApp {
        public final String name;
        public final String packageName;
        public final String category;
        public final String ageSuitability;
        public final String benefits;

        public RecommendedApp(String name, String packageName, String category, String ageSuitability, String benefits) {
            this.name = name;
            this.packageName = packageName;
            this.category = category;
            this.ageSuitability = ageSuitability;
            this.benefits = benefits;
        }
    }

    public static class AppRiskAssessment {
        public final String packageName;
        public final String appName;
        public final RiskLevel riskLevel;
        public final String reason;
        public final boolean hasLootBoxes;
        public final boolean hasUnmoderatedChat;

        public AppRiskAssessment(String packageName, String appName, RiskLevel riskLevel, String reason, boolean hasLootBoxes, boolean hasUnmoderatedChat) {
            this.packageName = packageName;
            this.appName = appName;
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.hasLootBoxes = hasLootBoxes;
            this.hasUnmoderatedChat = hasUnmoderatedChat;
        }
    }

    private static final List<RecommendedApp> CURATED_APPS = Collections.unmodifiableList(Arrays.asList(
            new RecommendedApp(
                    "Khan Academy Kids",
                    "org.khancademy.kids",
                    "Early Learning & Math",
                    "Ages 2–8",
                    "100% free, zero ads, foundational literacy, math, and logic puzzles."
            ),
            new RecommendedApp(
                    "ScratchJr",
                    "org.scratchjr.android",
                    "Coding & Creativity",
                    "Ages 5–10",
                    "Visual drag-and-drop programming empowering kids to create interactive stories and games."
            ),
            new RecommendedApp(
                    "Duolingo",
                    "com.duolingo",
                    "Languages & Linguistics",
                    "Ages 8+",
                    "Bite-sized, gamified language and math lessons designed for steady retention."
            ),
            new RecommendedApp(
                    "YouTube Kids",
                    "com.google.android.apps.youtube.kids",
                    "Curated Video & Science",
                    "Ages 3–12",
                    "Filtered entertainment and educational content with strict parental control profiles."
            ),
            new RecommendedApp(
                    "Chess for Kids",
                    "com.chess.kids",
                    "Tactics & Problem Solving",
                    "Ages 6+",
                    "Safe chess puzzles, animated tutorials, and cognitive reasoning practice."
            ),
            new RecommendedApp(
                    "NASA App",
                    "gov.nasa",
                    "Astronomy & STEM",
                    "Ages 8+",
                    "Space discoveries, high-definition satellite imagery, and live mission tracking."
            )
    ));

    // Signatures of high-risk keywords in package or app names
    private static final Map<String, String> HIGH_RISK_KEYWORDS = new HashMap<>();
    static {
        HIGH_RISK_KEYWORDS.put("casino", "Simulated gambling / coin betting mechanics.");
        HIGH_RISK_KEYWORDS.put("slot", "Slot machine mechanics designed to encourage repetitive purchases.");
        HIGH_RISK_KEYWORDS.put("poker", "Card betting and real-money gaming exposure.");
        HIGH_RISK_KEYWORDS.put("omegle", "Unmoderated anonymous video/text chat with strangers.");
        HIGH_RISK_KEYWORDS.put("roulette", "Random stranger chat / gambling exposure.");
        HIGH_RISK_KEYWORDS.put("loot", "High-frequency loot box microtransaction model.");
    }

    public List<RecommendedApp> getCuratedApps() {
        return CURATED_APPS;
    }

    /**
     * Evaluates the risk profile of an app using package name and label analysis.
     */
    public AppRiskAssessment assessAppRisk(String packageName, String appName) {
        String lowerPkg = (packageName != null ? packageName.toLowerCase() : "");
        String lowerName = (appName != null ? appName.toLowerCase() : "");

        for (Map.Entry<String, String> entry : HIGH_RISK_KEYWORDS.entrySet()) {
            if (lowerPkg.contains(entry.getKey()) || lowerName.contains(entry.getKey())) {
                boolean isChat = entry.getKey().equals("omegle") || entry.getKey().equals("roulette");
                boolean isGambling = !isChat;
                return new AppRiskAssessment(
                        packageName,
                        appName != null ? appName : packageName,
                        RiskLevel.HIGH_RISK,
                        entry.getValue(),
                        isGambling,
                        isChat
                );
            }
        }

        // Generic games that commonly contain aggressive in-app purchases
        if (lowerPkg.contains(".game.") || lowerPkg.contains(".games.") || lowerName.contains("clash") || lowerName.contains("crush") || lowerName.contains("royale")) {
            return new AppRiskAssessment(
                    packageName,
                    appName != null ? appName : packageName,
                    RiskLevel.CAUTION,
                    "Contains aggressive microtransactions and recurring store promotions.",
                    true,
                    false
            );
        }

        return new AppRiskAssessment(
                packageName,
                appName != null ? appName : packageName,
                RiskLevel.SAFE,
                "Standard application with no detected predatory mechanics.",
                false,
                false
        );
    }
}
