package com.toonetown.guava_ext.collect;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Wraps an iterator of entries, and only returns the non-expired ones.  Similar to guava's AbstractIterator or
 * ForwardingIterator, but this one supports the remove call.
 */
public class ExpiringIterator<T> implements Iterator<T> {

    /** Tracks the state of our iterator */
    private enum State { READY, NOT_READY, DONE, FAILED }
    private State state = State.NOT_READY;

    private final Iterator<Entry<T>> entryIterator;
    private Entry<T> next;

    protected ExpiringIterator(final Iterator<Entry<T>> entryIterator) {
        this.entryIterator = entryIterator;
    }

    @Override public boolean hasNext() {
        checkState(state != State.FAILED);
        switch (state) {
            case DONE: return false;
            case READY: return true;
            default:
                state = State.FAILED;
                next = nextUnexpired();
                if (next == null) {
                    state = State.DONE;
                    return false;
                } else {
                    state = State.READY;
                    return true;
                }
        }
    }

    @Override public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        return next.getItem();
    }

    @Override public void remove() {
        /* Just pass it through to the delegate */
        entryIterator.remove();
    }

    /**
     * Returns the next unexpired entry from our underlying iterator - removing any expired values along the way.
     * Returns null when the underlying iterator has no more entries
     */
    private Entry<T> nextUnexpired() {
        while (entryIterator.hasNext()) {
            final Entry<T> nextEntry = entryIterator.next();
            if (!nextEntry.isExpired()) {
                return nextEntry;
            }
            /* We were expired, so remove it and loop around */
            remove();
        }
        /* We have no more entries, so return null */
        return null;
    }

    /** A class which represents the item with expiration information added */
    @EqualsAndHashCode(of = { "item" })
    protected static class Entry<T> {
        @Getter private final T item;
        private final Stopwatch stopwatch;
        private final long expirationTime;
        private final TimeUnit expirationUnit;

        protected Entry(final T item,
                        final Ticker ticker,
                        final long expirationTime,
                        final TimeUnit expirationUnit) {
            checkArgument(expirationTime > 0);
            checkNotNull(expirationUnit);
            this.item = item;
            this.stopwatch = Stopwatch.createStarted(ticker);
            this.expirationTime = expirationTime;
            this.expirationUnit = expirationUnit;
        }

        protected void touch() { stopwatch.reset().start(); }
        protected boolean isExpired() { return (stopwatch.elapsed(expirationUnit) >= expirationTime); }
    }
}
