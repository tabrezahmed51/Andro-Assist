package com.giyhub.app;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppRecommendationManagerTest {

    @Test
    public void testCuratedAppsList() {
        AppRecommendationManager manager = new AppRecommendationManager();
        List<AppRecommendationManager.RecommendedApp> apps = manager.getCuratedApps();

        assertNotNull(apps);
        assertTrue(apps.size() >= 5);

        boolean hasKhan = false;
        boolean hasScratch = false;
        for (AppRecommendationManager.RecommendedApp app : apps) {
            if ("Khan Academy Kids".equals(app.name)) hasKhan = true;
            if ("ScratchJr".equals(app.name)) hasScratch = true;
        }
        assertTrue(hasKhan);
        assertTrue(hasScratch);
    }

    @Test
    public void testRiskAssessmentForGamblingAndLootboxes() {
        AppRecommendationManager manager = new AppRecommendationManager();

        // Gambling / Casino risk test
        AppRecommendationManager.AppRiskAssessment casinoRisk =
                manager.assessAppRisk("com.vegas.casino.slots", "Vegas Slots 777");
        assertEquals(AppRecommendationManager.RiskLevel.HIGH_RISK, casinoRisk.riskLevel);
        assertTrue(casinoRisk.hasLootBoxes);

        // Anonymous chat risk test
        AppRecommendationManager.AppRiskAssessment chatRisk =
                manager.assessAppRisk("com.random.omegle.chat", "Omegle Video Talk");
        assertEquals(AppRecommendationManager.RiskLevel.HIGH_RISK, chatRisk.riskLevel);
        assertTrue(chatRisk.hasUnmoderatedChat);

        // Caution game test
        AppRecommendationManager.AppRiskAssessment cautionRisk =
                manager.assessAppRisk("com.supercell.clashroyale", "Clash Royale");
        assertEquals(AppRecommendationManager.RiskLevel.CAUTION, cautionRisk.riskLevel);

        // Safe app test
        AppRecommendationManager.AppRiskAssessment safeApp =
                manager.assessAppRisk("org.wikipedia", "Wikipedia");
        assertEquals(AppRecommendationManager.RiskLevel.SAFE, safeApp.riskLevel);
        assertFalse(safeApp.hasLootBoxes);
        assertFalse(safeApp.hasUnmoderatedChat);
    }
}
