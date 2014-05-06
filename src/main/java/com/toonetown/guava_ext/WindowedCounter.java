package com.toonetown.guava_ext;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Counts the number of times a value is added.  This process is atomic (using AtomicLong), and self-cleaning.  The
 * type you use must be suitable for use as a key in a hashmap (via .equals()).  There are functions which mirror the
 * AtomicLong functions (but for a given object), as well as functions to operate on the cache itself (stats, size,
 * invalidation, cleanup) and a mechanism to get the current window of data as a MultiSet.
 */
@Slf4j
public class WindowedCounter<T> {
    /** The size of the window */
    private final long windowSize;

    /** The unit of the window */
    private final TimeUnit windowUnit;

    /** The stopwatch we use for tracking windows */
    private final Stopwatch stopwatch;

    /** The expiration multiplier */
    private final int expirationMultiplier;

    /** The underlying cache we will use */
    private final Cache<Key<T>, AtomicLong> cache;

    /** A class we use internally for keys to our cache */
    @Data private static final class Key<T> {
        final long window;
        final T item;
    }

    /**
     * Constructor which creates a WindowedCounter.  This class uses a cache for self-cleaning - entries are set to
     * expire after write, based on the expirationMultiplier.  For example, if the window size is 5, and
     * expirationMultiplier is 2, then entries will be expired every 10 (5x2) units.
     *
     * @param windowSize the size of the window
     * @param windowUnit the unit of the window
     * @param ticker a ticker to use for computing the window
     * @param expirationMultiplier a multiplier to use for expiring entries in the cache
     */
    private WindowedCounter(final long windowSize,
                            final TimeUnit windowUnit,
                            final Ticker ticker,
                            final int expirationMultiplier) {
        checkArgument(windowSize > 0);
        checkArgument(expirationMultiplier > 0);
        this.windowSize = windowSize;
        this.windowUnit = windowUnit;
        this.stopwatch = Stopwatch.createStarted(ticker);
        this.expirationMultiplier = expirationMultiplier;
        this.cache = Caches.newBuilder()
                           .expireAfterWrite(windowSize * expirationMultiplier, windowUnit)
                           .ticker(ticker)
                           .recordStats()
                           .build();
    }

    /** Returns the value of the current window */
    private long currentWindow() { return stopwatch.elapsed(windowUnit) / windowSize; }

    /** Returns a set of all possibly cached windows (including the current window) */
    private Set<Long> cachedWindows() {
        final long currentWindow = currentWindow();
        return ContiguousSet.create(Range.closed(Math.max(0, currentWindow - expirationMultiplier), currentWindow),
                                    DiscreteDomain.longs());
    }

    /** Returns the key into the cache to use */
    private Key<T> key(final T item) { return new Key<T>(currentWindow(), item); }

    /** Returns all the keys that may be in the cache for the given item (including previous windows) */
    private Iterable<Key<T>> allKeys(final T item) {
        return Iterables.transform(cachedWindows(), new Function<Long, Key<T>>() {
            @Override public Key<T> apply(final Long input) { return new Key<T>(input, item); }
        });
    }

    /** Returns an iterable of all possibly cached (including previous windows) for the given items */
    private Iterable<Key<T>> allKeys(final Iterable<T> items) {
        return Iterables.concat(Iterables.transform(items, new Function<T, Iterable<Key<T>>>() {
            @Override public Iterable<Key<T>> apply(final T input) { return allKeys(input); }
        }));
    }

    /**
     * Returns the value of the counter for the given item in the current window.  Modifying the returned value will
     * "write through" to the underlying data
     */
    public AtomicLong asAtomicLong(final T item) {
        try {
            return cache.get(key(item), new Callable<AtomicLong>() {
                @Override public AtomicLong call() { return new AtomicLong(); }
            });
        } catch (ExecutionException e) {
            log.warn("Unexpected exception getting from cache", e);
            return new AtomicLong();
        }
    }

    /**
     * Returns the values in this current window as an immutable multiset.
     */
    public ImmutableMultiset<T> asMultiset() {
        final ImmutableMultiset.Builder<T> builder = ImmutableMultiset.builder();
        synchronized (cache) {
            final long currentWindow = currentWindow();
            for (final Map.Entry<Key<T>, AtomicLong> entry : cache.asMap().entrySet()) {
                if (entry.getKey().window == currentWindow) {
                    builder.addCopies(entry.getKey().item, entry.getValue().intValue());
                }
            }
        }
        return builder.build();
    }

    /** These functions mirror the cache functions - but based off the given item */
    public CacheStats stats() { return cache.stats(); }
    public long size() { return cache.size(); }
    public void cleanUp() { cache.cleanUp(); }
    public void invalidate(final T item) { cache.invalidateAll(allKeys(item)); }
    public void invalidateAll(final Iterable<T> items) { cache.invalidateAll(allKeys(items)); }
    public void invalidateAll() { cache.invalidateAll(); }

    /** These functions mirror the AtomicLong functions, but based off the given item and the current window */
    public long get(final T item) { return asAtomicLong(item).get(); }
    public long addAndGet(final T item, final long delta) { return asAtomicLong(item).addAndGet(delta); }
    public long getAndAdd(final T item, final long delta) { return asAtomicLong(item).getAndAdd(delta); }
    public long decrementAndGet(final T item) { return asAtomicLong(item).decrementAndGet(); }
    public long getAndDecrement(final T item) { return asAtomicLong(item).getAndDecrement(); }
    public long incrementAndGet(final T item) { return asAtomicLong(item).incrementAndGet(); }
    public long getAndIncrement(final T item) { return asAtomicLong(item).getAndIncrement(); }
    public long getAndSet(final T item, final long newValue) { return asAtomicLong(item).getAndSet(newValue); }
    public void set(final T item, final long newValue) { asAtomicLong(item).set(newValue); }

    /**
     * Creates a counter with the given options.
     *
     * @param clazz the class of item this counter will count
     * @param windowSize the size of the window
     * @param windowUnit the unit of the window
     * @param ticker a ticker to use for computing the window
     * @param expirationMultiplier a multiplier to use for expiring entries in the cache
     * @return the created counter
     */
    public static <T> WindowedCounter<T> create(final Class<T> clazz,
                                                final long windowSize,
                                                final TimeUnit windowUnit,
                                                final Ticker ticker,
                                                final int expirationMultiplier) {
        return new WindowedCounter<T>(windowSize, windowUnit, ticker, expirationMultiplier);
    }
    /** Creates a counter with an expirationMultiplier of 2 */
    public static <T> WindowedCounter<T> create(final Class<T> clazz,
                                                final long windowSize,
                                                final TimeUnit windowUnit,
                                                final Ticker ticker) {
        return create(clazz, windowSize, windowUnit, ticker, 2);
    }
    /** Creates a counter with the system ticker */
    public static <T> WindowedCounter<T> create(final Class<T> clazz,
                                                final long windowSize,
                                                final TimeUnit windowUnit,
                                                final int expirationMultiplier) {
        return create(clazz, windowSize, windowUnit, Ticker.systemTicker(), expirationMultiplier);
    }
    /** Creates a counter with the system ticker and an expirationMultipler of 2 */
    public static <T> WindowedCounter<T> create(final Class<T> clazz,
                                                final long windowSize,
                                                final TimeUnit windowUnit) {
        return create(clazz, windowSize, windowUnit, Ticker.systemTicker(), 2);
    }
}
