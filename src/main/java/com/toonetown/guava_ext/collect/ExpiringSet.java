package com.toonetown.guava_ext.collect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * A set which expires its entries - similar to a cache.  Each entry can have its own expiration.  Like the hashMap
 * that backs this set, this is *not* threadsafe.
 */
public class ExpiringSet<T> extends AbstractSet<T> {

    /** Expiration modes */
    public enum Mode { AFTER_CREATION, AFTER_READD }

    /** A delegate set that actually holds the values */
    @Getter(AccessLevel.PROTECTED) @Accessors(fluent = true) private final Set<ExpiringIterator.Entry<T>> delegate;

    /** The default time to add entries for */
    private final long defaultExpireTime;

    /** The default unit to add entries for */
    private final TimeUnit defaultExpireUnit;

    /**
     * The expiration mode we operate in.  AFTER_CREATION will expire after the *first* time it was added.  AFTER_READD
     * will reset the expiration when you readd an existing entry.
     */
    @Getter private final Mode mode;

    /** A ticker to use for counting our expirations */
    private final Ticker ticker;

    private ExpiringSet(final Set<ExpiringIterator.Entry<T>> delegate,
                        final Mode mode,
                        final Ticker ticker,
                        final long defaultExpireTime,
                        final TimeUnit defaultExpireUnit) {
        this.delegate = delegate;
        this.mode = mode;
        this.ticker = ticker;
        this.defaultExpireTime = defaultExpireTime;
        this.defaultExpireUnit = defaultExpireUnit;
    }

    @Override public Iterator<T> iterator() { return new ExpiringIterator<T>(delegate().iterator()); }
    @Override public int size() { return Iterators.size(iterator()); }
    @Override public boolean add(final T item) {
        /** Adds with the default time and unit - unless we don't have a default time and unit */
        if (defaultExpireTime == 0 || defaultExpireUnit == null) {
            throw new UnsupportedOperationException();
        }
        return add(item, defaultExpireTime, defaultExpireUnit);
    }

    /** Adds an entry with the given expiration time and expiration unit */
    public boolean add(final T item, final long expireTime, final TimeUnit expireUnit) {
        final ExpiringIterator.Entry<T> entry = new ExpiringIterator.Entry<T>(item, ticker, expireTime, expireUnit);
        boolean readded = false;
        if (mode == Mode.AFTER_READD && delegate().contains(entry)) {
            /* Remove the existing one and update it */
            delegate().remove(entry);
            readded = true;
        }
        return delegate.add(entry) && !readded;
    }

    /** Static creator function - backed by a HashSet */
    public static <T> ExpiringSet<T> create(final Mode mode,
                                            final Ticker ticker,
                                            final long defaultExpireTime,
                                            final TimeUnit defaultExpireUnit) {
        return new ExpiringSet<T>(Sets.<ExpiringIterator.Entry<T>>newHashSet(),
                                  mode,
                                  ticker,
                                  defaultExpireTime,
                                  defaultExpireUnit);
    }
    public static <T> ExpiringSet<T> create(final Mode mode,
                                            final long defaultExpireTime,
                                            final TimeUnit defaultExpireUnit) {
        return create(mode, Ticker.systemTicker(), defaultExpireTime, defaultExpireUnit);
    }
    public static <T> ExpiringSet<T> create(final Mode mode, final Ticker ticker) {
        return create(mode, ticker, 0, null);
    }
    public static <T> ExpiringSet<T> create(final Mode mode) {
        return create(mode, Ticker.systemTicker());
    }

}
