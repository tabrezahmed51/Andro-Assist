package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ScreenTimeHelperTest {

    @Test
    public void testFormatDuration() {
        assertEquals("0h 00m", ScreenTimeHelper.formatDuration(0));
        assertEquals("0h 45m", ScreenTimeHelper.formatDuration(45));
        assertEquals("1h 30m", ScreenTimeHelper.formatDuration(90));
        assertEquals("2h 05m", ScreenTimeHelper.formatDuration(125));
    }

    @Test
    public void testCalculateProgress() {
        assertEquals(0, ScreenTimeHelper.calculateProgress(0, 120));
        assertEquals(50, ScreenTimeHelper.calculateProgress(60, 120));
        assertEquals(100, ScreenTimeHelper.calculateProgress(120, 120));
        assertEquals(100, ScreenTimeHelper.calculateProgress(150, 120));
        assertEquals(0, ScreenTimeHelper.calculateProgress(50, 0)); // zero limit guard
    }

    @Test
    public void testCalculateRemaining() {
        assertEquals(120, ScreenTimeHelper.calculateRemaining(0, 120));
        assertEquals(60, ScreenTimeHelper.calculateRemaining(60, 120));
        assertEquals(0, ScreenTimeHelper.calculateRemaining(120, 120));
        assertEquals(0, ScreenTimeHelper.calculateRemaining(150, 120)); // floor at 0
    }
}
