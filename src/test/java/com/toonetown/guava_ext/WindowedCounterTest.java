package com.toonetown.guava_ext;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableSet;

import com.toonetown.guava_ext.testing.DataProviders;
import com.toonetown.guava_ext.testing.ManualTicker;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit tests for WindowedCounter
 */
public class WindowedCounterTest {

    public static DataProviders.ParameterList createCounter(final int expirationMultiplier) {
        final Ticker ticker = new ManualTicker();
        if (expirationMultiplier > 0) {
            return params(WindowedCounter.create(String.class, 5, TimeUnit.SECONDS, ticker, expirationMultiplier),
                          ticker,
                          expirationMultiplier);
        }
        return params(WindowedCounter.create(String.class, 5, TimeUnit.SECONDS, ticker), ticker, 2);
    }

    public static DataProviders.TestSet getCounterData() {
        return tests(createCounter(0), createCounter(1), createCounter(2), createCounter(3));
    }

    @DataProvider(name = "counterData", parallel = true)
    public Object[][] counterData() { return getCounterData().create(); }


    private void assertCounterStats(final WindowedCounter<String> counter,
                                    final long size,
                                    final long hitCount,
                                    final long missCount,
                                    final long evictionCount) {
        assertEquals(counter.size(), size);
        final CacheStats stats = counter.stats();
        assertEquals(stats.hitCount(), hitCount);
        assertEquals(stats.missCount(), missCount);
        assertEquals(stats.evictionCount(), evictionCount);
    }

    @Test(dataProvider = "counterData")
    public void testWindowedCounter(final WindowedCounter<String> counter,
                                    final ManualTicker ticker,
                                    final int expirationMultiplier) {
        assertCounterStats(counter, 0, 0, 0, 0);
        assertEquals(counter.incrementAndGet("abc"), 1);
        assertEquals(counter.incrementAndGet("def"), 1);
        assertEquals(counter.incrementAndGet("abc"), 2);
        assertCounterStats(counter, 2, 1, 2, 0);
    }

    @Test(dataProvider = "counterData")
    public void testTicking(final WindowedCounter<String> counter,
                            final ManualTicker ticker,
                            final int expirationMultiplier) {
        counter.incrementAndGet("abc");
        counter.incrementAndGet("def");
        counter.incrementAndGet("abc");
        assertCounterStats(counter, 2, 1, 2, 0);

        /* Same window */
        ticker.tick(4, TimeUnit.SECONDS);
        assertEquals(counter.incrementAndGet("def"), 2);
        assertCounterStats(counter, 2, 2, 2, 0);

        /* Next window */
        ticker.tick(1, TimeUnit.SECONDS);
        assertEquals(counter.incrementAndGet("abc"), 1);
        assertEquals(counter.incrementAndGet("def"), 1);
        assertEquals(counter.incrementAndGet("ghi"), 1);

        /* Call cleanup here so that we can check our "real" counters */
        counter.cleanUp();
        final int expAdj = expirationMultiplier == 1 ? 2 : 0;
        assertCounterStats(counter, 5 - expAdj, 2, 5, expAdj);
    }

    @Test(dataProvider = "counterData")
    public void testEviction(final WindowedCounter<String> counter,
                             final ManualTicker ticker,
                             final int expirationMultiplier) {
        counter.incrementAndGet("abc");
        assertCounterStats(counter, 1, 0, 1, 0);

        for (int i = 0; i < 10; i++) {
            ticker.tick(5, TimeUnit.SECONDS);
            assertEquals(counter.incrementAndGet("abc"), 1);

            /* Call cleanup here so that we can check our "real" counters */
            counter.cleanUp();
            assertCounterStats(counter,
                               Math.min(2 + i, expirationMultiplier),
                               0,
                               2 + i,
                               Math.max(0, 2 + i - expirationMultiplier));
        }
    }

    @Test(dataProvider = "counterData")
    public void testInvalidate(final WindowedCounter<String> counter,
                               final ManualTicker ticker,
                               final int expirationMultiplier) {
        counter.incrementAndGet("abc");
        counter.incrementAndGet("def");
        counter.incrementAndGet("abc");
        assertCounterStats(counter, 2, 1, 2, 0);

        /* Same window */
        ticker.tick(4, TimeUnit.SECONDS);
        counter.incrementAndGet("def");
        counter.incrementAndGet("ghi");
        assertCounterStats(counter, 3, 2, 3, 0);

        /* Next window */
        ticker.tick(1, TimeUnit.SECONDS);
        counter.incrementAndGet("abc");
        counter.incrementAndGet("def");
        counter.incrementAndGet("ghi");

        /* Call cleanup here so that we can check our "real" counters */
        counter.cleanUp();
        final int expAdj = expirationMultiplier == 1 ? 2 : 0;
        assertCounterStats(counter, 6 - expAdj, 2, 6, expAdj);

        counter.invalidate("abc");
        assertCounterStats(counter, 4 - Math.max(0, expAdj - 1), 2, 6, expAdj);
        counter.invalidate("def");
        assertCounterStats(counter, 2, 2, 6, expAdj);
        counter.invalidate("ghi");
        assertCounterStats(counter, 0, 2, 6, expAdj);
        counter.invalidate("jkl");
        assertCounterStats(counter, 0, 2, 6, expAdj);
    }

    @Test(dataProvider = "counterData")
    public void testInvalidateAll(final WindowedCounter<String> counter,
                                  final ManualTicker ticker,
                                  final int expirationMultiplier) {
        counter.incrementAndGet("abc");
        counter.incrementAndGet("def");
        counter.incrementAndGet("abc");
        assertCounterStats(counter, 2, 1, 2, 0);

        /* Same window */
        ticker.tick(4, TimeUnit.SECONDS);
        counter.incrementAndGet("def");
        counter.incrementAndGet("ghi");
        assertCounterStats(counter, 3, 2, 3, 0);

        /* Next window */
        ticker.tick(1, TimeUnit.SECONDS);
        counter.incrementAndGet("abc");
        counter.incrementAndGet("def");
        counter.incrementAndGet("ghi");

        /* Call cleanup here so that we can check our "real" counters */
        counter.cleanUp();
        final int expAdj = expirationMultiplier == 1 ? 2 : 0;
        assertCounterStats(counter, 6 - expAdj, 2, 6, expAdj);

        counter.invalidateAll(ImmutableSet.of("abc", "def", "jkl"));
        assertCounterStats(counter, 2, 2, 6, expAdj);
    }

}
