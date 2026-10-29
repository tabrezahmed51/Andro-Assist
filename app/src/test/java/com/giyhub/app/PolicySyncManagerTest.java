package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PolicySyncManagerTest {

    @Test
    public void testPolicySerializationAndDeserialization() {
        PolicySyncManager.DevicePolicy policy = new PolicySyncManager.DevicePolicy(
                "FAM-TEST",
                "DEV-123",
                150,
                true,
                22,
                6,
                30
        );

        String json = PolicySyncManager.serializePolicy(policy);
        assertNotNull(json);
        assertTrue(json.contains("FAM-TEST"));
        assertTrue(json.contains("DEV-123"));

        PolicySyncManager.DevicePolicy restored = PolicySyncManager.deserializePolicy(json);
        assertEquals("FAM-TEST", restored.familyId);
        assertEquals("DEV-123", restored.deviceId);
        assertEquals(150, restored.dailyLimitMinutes);
        assertTrue(restored.instantPauseActive);
        assertEquals(22, restored.bedtimeStartHour);
        assertEquals(6, restored.bedtimeEndHour);
        assertEquals(30, restored.bonusMinutes);
    }
}
