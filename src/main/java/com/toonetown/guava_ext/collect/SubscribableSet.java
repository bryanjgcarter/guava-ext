package com.toonetown.guava_ext.collect;

import lombok.Getter;
import lombok.Synchronized;

import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.toonetown.guava_ext.AutoLock;
import com.toonetown.guava_ext.eventbus.Events;
import com.toonetown.guava_ext.eventbus.Publish;
import com.toonetown.guava_ext.eventbus.Subscribable;
import com.toonetown.guava_ext.eventbus.ThreadsafeEventBus;

/**
 * A ForwardingSet which will public add and remove elements when it is modified.
 *
 * This class posts the following events:
 *   <ul><li><b>Events.AddEvent&lt;SubscribableSet, E&gt;</b> <i>Sourced to: this</i> - when an item is added to the
 *           set</li>
 *       <li><b>Events.RemoveEvent&lt;SubscribableSet, E&gt;</b> <i>Sourced to: this</i> - when an item is removed
 *           from the set</li></ul>
 */
public abstract class SubscribableSet<E> extends ForwardingSet<E> implements Subscribable {
    /** The EventBus instance we will use to communicate with others */
    @Getter private final EventBus eventBus = new ThreadsafeEventBus(getClass().getName());
    
    /** Posts our add event */
    @Publish(Events.AddEvent.class)
    protected void postAddEvent(final E element) { eventBus.post(Events.newAddEvent(this, element)); }

    /** Posts our remove event */
    @Publish(Events.RemoveEvent.class)
    protected void postRemoveEvent(final E element) { eventBus.post(Events.newRemoveEvent(this, element)); }

    /**
     * This function will get called whenever an iterator's remove gets called
     */
    @Subscribe public void onIteratorRemoveEvent(final Events.RemoveEvent<SubscribableIterator<E>, E> event) {
        postRemoveEvent(event.element());
    }
    
    @Override protected abstract Set<E> delegate();

    /** {@inheritDoc} Overridden to post the add event */
    @Override public boolean add(final E e) {
        final boolean changed = super.add(e);
        if (changed) {
            postAddEvent(e);
        }
        return changed;
    }
    
    @Override public Iterator<E> iterator() { return delegateIterator(); }

    /** {@inheritDoc} Overridden to call the standards */
    @Override public boolean addAll(final Collection<? extends E> c) { return standardAddAll(c); }
    /** {@inheritDoc} Overridden to call the standards */
    @Override public void clear() { standardClear(); }
    /** {@inheritDoc} Overridden to call the standards */
    @Override public boolean remove(final Object o) { return standardRemove(o); }
    /** {@inheritDoc} Overridden to call the standards */
    @Override public boolean removeAll(final Collection<?> c) { return standardRemoveAll(c); }
    /** {@inheritDoc} Overridden to call the standards */
    @Override public boolean retainAll(final Collection<?> c) { return standardRetainAll(c); }

    /* Cast register/unregister to return a SubscribableSet */
    @Override public SubscribableSet<E> register(final Object o) { eventBus.register(o); return this; }
    @Override public SubscribableSet<E> unregister(final Object o) { eventBus.unregister(o); return this; }

    /** {@inheritDoc} Overridden to user our delegateIterator */
    @Override protected boolean standardRemove(final Object o) {
        try {
            final Iterator<E> i = delegateIterator();
            while (i.hasNext()) {
                if (Objects.equal(i.next(), o)) {
                    i.remove();
                    return true;
                }
            }
            return false;
        } finally {
            delegate().remove(o);
        }
    }
    /** {@inheritDoc} Overridden to user our delegateIterator */
    @Override protected boolean standardRemoveAll(final Collection<?> c) {
        try {
            return Iterators.removeAll(delegateIterator(), c);
        } finally {
            delegate().removeAll(c);
        }
    }
    /** {@inheritDoc} Overridden to user our delegateIterator */
    @Override protected boolean standardRetainAll(final Collection<?> c) {
        try {
            return Iterators.retainAll(delegateIterator(), c);
        } finally {
            delegate().retainAll(c);
        }
    }
    /** {@inheritDoc} Overridden to user our delegateIterator */
    @Override protected void standardClear() {
        try {
            final Iterator<E> i = delegateIterator();
            while (i.hasNext()) { i.next(); i.remove(); }
        } finally {
            delegate().clear();
        }
    }

    /**
     * Directly returns the delegate's iterator.  This is so subclasses can replace the iterator(), but our 
     * implementation functions (addAll, remove, etc) can all operate on the original backing iterator.  This is
     * wrapped in a SubscribableIterator so that we get notifications of removals.
     */
    protected Iterator<E> delegateIterator() {
        return SubscribableIterator.wrap(super.iterator()).register(this);
    }

    /**
     * A static creator function.
     *
     * @param backingSet the set that you want to be subscribable.
     * @return the SubscribableSet
     */
    @SuppressWarnings("unchecked")
    public static <E> SubscribableSet<E> wrap(final Set<? extends E> backingSet) {
        if (backingSet instanceof SubscribableImpl) {
            return (SubscribableImpl<E>) backingSet;
        }
        return new SubscribableImpl<>((Set<E>) backingSet);
    }

    /**
     * A static creator function.  This version behaves like a CopyOnWriteArraySet in that it is safe to use in 
     * multiple threads.
     *
     * @param backingSet the set that you want to be subscribable and thread safe.
     * @return the SubscribableSet
     */
    @SuppressWarnings("unchecked")
    public static <E> SubscribableSet<E> wrapSafe(final Set<? extends E> backingSet) {
        if (backingSet instanceof ThreadsafeSubscribableImpl) {
            return (ThreadsafeSubscribableImpl<E>) backingSet;
        }
        return new ThreadsafeSubscribableImpl<>((Set<E>) backingSet);
    }

    /**
     * A private implementation
     */
    private static class SubscribableImpl<E> extends SubscribableSet<E> {
        private final Set<E> delegate;
        @Override protected Set<E> delegate() { return delegate; }
        private SubscribableImpl(final Set<E> delegate) { this.delegate = delegate; }
    }
    
    /**
     * A private implementation that has a thread safe iterator
     */
    private static class ThreadsafeSubscribableImpl<E> extends SubscribableImpl<E> {
        private ThreadsafeSubscribableImpl(final Set<E> delegate) { super(delegate); }
        
        /** A lock around our iteratorSet */
        transient final ReentrantLock lock = new ReentrantLock();
        /** A set which provides our iterator */
        transient Set<E> iteratorSet;
        
        /** {@inheritDoc} Overridden to return our cached iterator instead */
        @Override public Iterator<E> iterator() {
            try (final AutoLock ignored = new AutoLock(lock)) {
                if (iteratorSet == null) {
                    iteratorSet = ImmutableSet.copyOf(delegate());
                }
                return iteratorSet.iterator();
            }
        }
        
        /** {@inheritDoc} Overridden to clear our cached iterator */
        @Override public void postAddEvent(final E item) {
            super.postAddEvent(item);
            try (final AutoLock ignored = new AutoLock(lock)) {
                iteratorSet = null;
            }
        }

        /** {@inheritDoc} Overridden to clear our cached iterator */
        @Override public void postRemoveEvent(final E item) {
            super.postRemoveEvent(item);
            try (final AutoLock ignored = new AutoLock(lock)) {
                iteratorSet = null;
            }
        }

        /** {@inheritDoc} Overridden to be synchronized */
        @Override @Synchronized protected boolean standardRemove(final Object o) {
            return super.standardRemove(o);
        }
        /** {@inheritDoc} Overridden to be synchronized */
        @Override @Synchronized protected boolean standardRemoveAll(final Collection<?> c) {
            return super.standardRemoveAll(c);
        }
        /** {@inheritDoc} Overridden to be synchronized */
        @Override @Synchronized protected boolean standardRetainAll(final Collection<?> c) {
            return super.standardRetainAll(c);
        }
        /** {@inheritDoc} Overridden to be synchronized */
        @Override @Synchronized protected void standardClear() {
            super.standardClear();
        }
    }
}
