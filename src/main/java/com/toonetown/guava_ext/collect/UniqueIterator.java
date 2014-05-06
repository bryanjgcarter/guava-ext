package com.toonetown.guava_ext.collect;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.Multiset;

/**
 * An iterator which only returns unique entries from the wrapped iterator.  Results will be returned in the same order
 * as the original iterator, but duplicates will not be returned.  The wrapped iterator is only consumed as this
 * iterator is.
 */
public class UniqueIterator<E> extends AbstractIterator<E> {
    
    /** The iterator we are wrapping */
    private final Iterator<E> backingIterator;
    
    /** A set to keep track of what we've already returned */
    private final Set<E> dupes = Sets.newHashSet();
    
    @Override protected E computeNext() {
        while (backingIterator.hasNext()) {
            final E next = backingIterator.next();
            if (!dupes.contains(next)) {
                dupes.add(next);
                return next;
            }
        }
        return endOfData();
    }
    
    /**
     * A function which wraps an iterator and makes it unique
     */
    @SuppressWarnings("unchecked")
    public static <E> UniqueIterator<E> wrap(final Iterator<? extends E> backingIterator) {
        if (backingIterator instanceof UniqueIterator) {
            return (UniqueIterator<E>) backingIterator;
        }
        return new UniqueIterator(backingIterator);
    }

    /**
     * A function which takes an Iterable and returns a UniqueIterator.  This function is smart enough not to 
     * create a new object on Set or MultiSet.
     */
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> from(final Iterable<? extends E> iterable) {
        if (iterable instanceof Multiset || iterable instanceof Set) {
            return (Iterator<E>) iterable.iterator();
        }
        return wrap(iterable.iterator());
    }

    /**
     * Our constructor
     */
    private UniqueIterator(final Iterator<E> backingIterator) {
        this.backingIterator = backingIterator;
    }
}
