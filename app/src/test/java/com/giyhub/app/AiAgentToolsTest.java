package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AiAgentToolsTest {

    @Test
    public void testSystemToolsPromptContainsTools() {
        String prompt = AiAgentTools.getSystemToolsPrompt();
        assertNotNull(prompt);
        assertTrue(prompt.contains("check_screen_time"));
        assertTrue(prompt.contains("set_daily_quota"));
        assertTrue(prompt.contains("toggle_instant_pause"));
        assertTrue(prompt.contains("toggle_sentinel"));
        assertTrue(prompt.contains("check_bedtime"));
        assertTrue(prompt.contains("award_bonus"));
    }
}
