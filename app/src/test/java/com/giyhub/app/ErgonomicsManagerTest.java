package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ErgonomicsManagerTest {

    @Test
    public void testEyeStrainPrompts() {
        assertFalse(ErgonomicsManager.shouldPromptEyeBreak(0));
        assertFalse(ErgonomicsManager.shouldPromptEyeBreak(15));
        assertTrue(ErgonomicsManager.shouldPromptEyeBreak(20));
        assertFalse(ErgonomicsManager.shouldPromptEyeBreak(25));
        assertTrue(ErgonomicsManager.shouldPromptEyeBreak(40));
        assertTrue(ErgonomicsManager.shouldPromptEyeBreak(60));
    }
}
