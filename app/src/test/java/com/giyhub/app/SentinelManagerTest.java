package com.giyhub.app;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SentinelManagerTest {

    @Test
    public void testSafeSearchAppends() {
        SentinelManager manager = new SentinelManager(androidx.test.core.app.ApplicationProvider.getApplicationContext());
        manager.setFilterEnabled(true);

        String googleUrl = "https://www.google.com/search?q=test";
        String safeGoogle = manager.appendSafeSearchParameters(googleUrl);
        assertTrue(safeGoogle.contains("safe=active"));

        String bingUrl = "https://www.bing.com/search?q=test";
        String safeBing = manager.appendSafeSearchParameters(bingUrl);
        assertTrue(safeBing.contains("adlt=strict"));
    }

    @Test
    public void testDomainRestrictionPatterns() {
        SentinelManager manager = new SentinelManager(androidx.test.core.app.ApplicationProvider.getApplicationContext());
        manager.setFilterEnabled(true);

        assertTrue(manager.isDomainRestricted("https://bad-adult-site.com"));
        assertTrue(manager.isDomainRestricted("http://online-casino-gambling.com"));
        assertTrue(manager.isDomainRestricted("bet365.com"));

        assertFalse(manager.isDomainRestricted("https://wikipedia.org"));
        assertFalse(manager.isDomainRestricted("https://khanacademy.org"));
        assertFalse(manager.isDomainRestricted("https://google.com"));
    }
}
