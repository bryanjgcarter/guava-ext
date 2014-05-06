package com.toonetown.guava_ext.collect;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;

import com.toonetown.guava_ext.testing.DataProviders;
import com.toonetown.guava_ext.testing.ManualTicker;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit test for expiring sets
 */
public class ExpiringSetTest {

    public static ExpiringSet<String> createSet(final ExpiringSet.Mode mode, final ManualTicker ticker) {
        return ExpiringSet.<String>create(mode, ticker, 5, TimeUnit.SECONDS);
    }
    public static DataProviders.TestSet getModes() {
        return tests(params(ExpiringSet.Mode.AFTER_CREATION, new ManualTicker()),
                     params(ExpiringSet.Mode.AFTER_READD, new ManualTicker()));
    }

    @DataProvider(name = "modes", parallel = true)
    public Object[][] modes() { return getModes().create(); }

    @Test(dataProvider = "modes")
    public void testExpiringSet(final ExpiringSet.Mode mode, final ManualTicker ticker) {
        final ExpiringSet<String> set = createSet(mode, ticker);
        assertEquals(set, Sets.<String>newHashSet());
        set.add("a");
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));

        /* Tick some */
        ticker.tick(4, TimeUnit.SECONDS);
        assertEquals(set, Sets.newHashSet("b", "c", "a"));
        set.add("d");
        assertEquals(set, Sets.newHashSet("b", "c", "a", "d"));

        /* Some should expire */
        ticker.tick(1, TimeUnit.SECONDS);
        assertTrue(set.contains("d"));
        assertFalse(set.contains("a"));
        assertEquals(set, Sets.newHashSet("d"));
        ticker.tick(5, TimeUnit.SECONDS);
        assertEquals(set, Sets.<String>newHashSet());
    }

    @Test(dataProvider = "modes")
    public void testExplicitRemoval(final ExpiringSet.Mode mode, final ManualTicker ticker) {
        final ExpiringSet<String> set = createSet(mode, ticker);
        assertEquals(set, Sets.<String>newHashSet());
        set.add("a");
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));

        /* Tick some */
        ticker.tick(4, TimeUnit.SECONDS);
        assertEquals(set, Sets.newHashSet("b", "c", "a"));
        set.add("d");
        set.remove("b");
        assertEquals(set, Sets.newHashSet("c", "a", "d"));
    }

    @Test(dataProvider = "modes")
    public void testExplicitExpiration(final ExpiringSet.Mode mode, final ManualTicker ticker) {
        final ExpiringSet<String> set = createSet(mode, ticker);
        assertEquals(set, Sets.<String>newHashSet());
        set.add("a", 3, TimeUnit.SECONDS);
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));

        /* Tick some */
        ticker.tick(4, TimeUnit.SECONDS);
        assertEquals(set, Sets.newHashSet("b", "c"));
        set.add("d");
        set.remove("b");
        assertEquals(set, Sets.newHashSet("c", "d"));
    }

    @Test(dataProvider = "modes")
    public void testReadd(final ExpiringSet.Mode mode, final ManualTicker ticker) {
        final ExpiringSet<String> set = createSet(mode, ticker);
        assertTrue(set.add("a"));
        assertFalse(set.add("a"));

        /* Tick some */
        ticker.tick(4, TimeUnit.SECONDS);
        assertFalse(set.add("a"));
        assertEquals(set, Sets.newHashSet("a"));

        /* And should expire - for after creation expiration */
        ticker.tick(1, TimeUnit.SECONDS);
        if (mode == ExpiringSet.Mode.AFTER_CREATION) {
            assertEquals(set, Sets.<String>newHashSet());
            assertTrue(set.add("a"));
        } else {
            assertEquals(set, Sets.newHashSet("a"));
            assertFalse(set.add("a"));
        }
        assertEquals(set, Sets.newHashSet("a"));
    }

}
