package com.toonetown.guava_ext.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.ForwardingIterator;

/**
 * An iterator which tracks its previous value - so can can call "last".  This is similar to a PeekingIterator, but
 * actually returns the same value as was last returned by "next()".
 */
public class LookBackIterator<T> extends ForwardingIterator<T> {
    /** Whether or not we have loaded the last value yet */
    private boolean hasLoaded;

    /** Our last value */
    private T last;

    /** Our constructor */
    private LookBackIterator(final Iterator<T> delegate) { this.delegate = delegate; }

    /** Our delegate iterator */
    private final Iterator<T> delegate;
    @Override protected Iterator<T> delegate() { return delegate; }

    @Override public T next() {
        /* Store the one before we return it */
        last = super.next();
        hasLoaded = true;
        return last;
    }

    /**
     * Returns the value last returned by next().  Throws NoSuchElementException if next() has not been called.
     * @return the value last returned by next().
     */
    public T last() throws NoSuchElementException {
        if (!hasLoaded) { throw new NoSuchElementException("Have not yet called next()"); }
        return last;
    }

    /**
     * Returns the value of last() - but first calls next() if next() has not been called.
     * @return the value last returned by next().
     */
    public T lastOrNext() { return hasLoaded ? last() : next(); }

    /**
     * Wraps an iterator as a LookBackIterator.  May return the same iterator if it is already a LookBackIterator.
     */
    public static <T> LookBackIterator<T> wrap(final Iterator<T> delegate) {
        if (delegate instanceof LookBackIterator) { return (LookBackIterator<T>) delegate; }
        return new LookBackIterator<>(delegate);
    }
}
