package com.toonetown.guava_ext;

import lombok.Data;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Ticker;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A base class which tracks statistics for loading information.
 */
@Data @Accessors(fluent = true)
public class LoadStats {
    /** The base unit that we actually store values in */
    private static final TimeUnit BASE_UNIT = TimeUnit.NANOSECONDS;
    
    /** The number of successful loads */
    private final long loadSuccessCount;

    /** The number of timeouts during load */
    private final long loadTimeoutCount;

    /** The number of exception load */
    private final long loadExceptionCount;
    
    /** The total number of loads (success, timeout, and exception) */
    public long loadCount() { return loadSuccessCount + loadTimeoutCount + loadExceptionCount; }

    /** The total time (in the given time unit) that has been spent loading */
    @Getter(AccessLevel.NONE) private final long totalLoadTime;
    public long totalLoadTime(final TimeUnit unit) { return unit.convert(totalLoadTime, BASE_UNIT); }

    /** The percent of load requests that resulted in a timeout, or 0 if loadCount == 0 */
    public double loadTimeoutRate() { return rate(loadTimeoutCount, loadCount(), 0.0); }

    /** The percent of load requests that resulted in an exception, or 0 if loadCount == 0 */
    public double loadExceptionRate() { return rate(loadExceptionCount, loadCount(), 0.0); }
    
    /** The average time spent on each load, or 0 if loadCount == 0 */
    public double averageLoadTime(final TimeUnit unit) { return rate(totalLoadTime(unit), loadCount(), 0.0); }
    
    /**
     * Returns a new LoadStats representing the difference between this object and the other one. Negative
     * values are rounded up to zero.  Subclasses will want to override this.
     */
    public LoadStats minus(final LoadStats stats) {
        return new LoadStats(nonNeg(loadSuccessCount - stats.loadSuccessCount),
                             nonNeg(loadTimeoutCount - stats.loadTimeoutCount),
                             nonNeg(loadExceptionCount - stats.loadExceptionCount),
                             nonNeg(totalLoadTime - stats.totalLoadTime),
                             BASE_UNIT);
    }
    
    /**
     * Returns a new LoadStats representing the aggregation of this object and the other one.  Subclasses will want
     * to override this.
     */
    public LoadStats plus(final LoadStats stats) {
        return new LoadStats(loadSuccessCount + stats.loadSuccessCount,
                             loadTimeoutCount + stats.loadTimeoutCount,
                             loadExceptionCount + stats.loadExceptionCount,
                             totalLoadTime + stats.totalLoadTime,
                             BASE_UNIT);
    }
    
    /**
     * Constructs a LoadStats object
     */
    public LoadStats(final long loadSuccessCount,
                     final long loadTimeoutCount,
                     final long loadExceptionCount,
                     final long totalLoadTime,
                     final TimeUnit unit) {
        this.loadSuccessCount = check(loadSuccessCount);
        this.loadTimeoutCount = check(loadTimeoutCount);
        this.loadExceptionCount = check(loadExceptionCount);
        this.totalLoadTime = BASE_UNIT.convert(check(totalLoadTime), unit);
    }
    /**
     * Constructs a LoadStats from another LoadStats
     */
    protected LoadStats(final LoadStats stats) {
        this(stats.loadSuccessCount, stats.loadTimeoutCount, stats.loadExceptionCount, stats.totalLoadTime, BASE_UNIT);
    }
    
    /**
     * Constructs an empty LoadStats object
     */
    public static LoadStats empty() { return new LoadStats(0, 0, 0, 0, BASE_UNIT); }
    
    /**
     * Subclasses can call this to return a rate of two values (num / total).  If total == 0, then ifZero is returned
     */
    protected static double rate(final long num, final long total, final double ifZero) {
        return (total == 0) ? ifZero : (double) num / total;
    }
    
    /**
     * Subclasses can call this to make sure a value is non-negative
     */
    protected static long check(final long value) { checkArgument(value >= 0); return value; }
    
    /**
     * Subclasses can call this to get a non-negative value
     */
    protected static long nonNeg(final long value) { return Math.max(0, value); }

    /** An interface which indicates that a particular class provides a getStats() function */
    public interface Measurable { LoadStats getStats(); }

    /**
     * A static class that can be used for counting stats in a thread-safe way.
     */
    public static class Counter {
        private final ImmutableSet<Class> timeoutClasses;
        private final AtomicLong loadSuccessCount = new AtomicLong(0);
        private final AtomicLong loadTimeoutCount = new AtomicLong(0);
        private final AtomicLong loadExceptionCount = new AtomicLong(0);
        private final AtomicLong totalLoadTime = new AtomicLong(0);
        private final Ticker ticker;

        public Stopwatch startLoading() { return Stopwatch.createStarted(ticker); }

        private void stopLoading(final Stopwatch stopwatch) {
            if (stopwatch != null && stopwatch.isRunning()) {
                recordLoadTime(nonNeg(stopwatch.stop().elapsed(BASE_UNIT)), BASE_UNIT);
            }
        }

        public Counter recordLoadSuccess(final Stopwatch stopwatch) {
            stopLoading(stopwatch);
            loadSuccessCount.incrementAndGet();
            return this;
        }
        private Counter recordLoadTimeout(final Stopwatch stopwatch) {
            stopLoading(stopwatch);
            loadTimeoutCount.incrementAndGet(); return this;
        }
        private Counter recordLoadException(final Stopwatch stopwatch) {
            stopLoading(stopwatch);
            loadExceptionCount.incrementAndGet(); return this;
        }
        public Counter recordLoadException(final Stopwatch stopwatch, final Throwable e) {
            for (final Class<?> clazz : timeoutClasses) {
                if (Casting.subclass(e.getClass(), clazz).isPresent()) {
                    return recordLoadTimeout(stopwatch);
                }
            }
            return recordLoadException(stopwatch);
        }

        protected Counter recordLoadTime(final long time, final TimeUnit unit) {
            totalLoadTime.addAndGet(BASE_UNIT.convert(time, unit));
            return this;
        }
        
        /** Increments this counter by the values in the given stats */
        public Counter increment(final LoadStats stats) {
            loadSuccessCount.addAndGet(stats.loadSuccessCount);
            loadTimeoutCount.addAndGet(stats.loadTimeoutCount);
            loadExceptionCount.addAndGet(stats.loadExceptionCount);
            totalLoadTime.addAndGet(stats.totalLoadTime);
            return this;
        }
        
        public Counter(final Ticker ticker, final Class... timeoutClasses) {
            this.ticker = ticker;
            this.timeoutClasses = ImmutableSet.copyOf(timeoutClasses);
        }
        public Counter(final Class... timeoutClasses) { this(Ticker.systemTicker(), timeoutClasses); }
        
        /** Returns a stats object for this counter */
        public LoadStats snapshot() {
            return new LoadStats(loadSuccessCount.get(),
                                 loadTimeoutCount.get(),
                                 loadExceptionCount.get(),
                                 totalLoadTime.get(),
                                 BASE_UNIT);
        }
        
        /** Resets this counter, returning the current stats as a snapshot */
        public LoadStats reset() {
            return new LoadStats(loadSuccessCount.getAndSet(0),
                                 loadTimeoutCount.getAndSet(0),
                                 loadExceptionCount.getAndSet(0),
                                 totalLoadTime.getAndSet(0),
                                 BASE_UNIT);
        }        
    }
}
