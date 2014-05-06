package com.toonetown.guava_ext.collect;

import lombok.Getter;

import java.util.Iterator;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingIterator;
import com.google.common.eventbus.EventBus;

import com.toonetown.guava_ext.eventbus.Events;
import com.toonetown.guava_ext.eventbus.Publish;
import com.toonetown.guava_ext.eventbus.Subscribable;
import com.toonetown.guava_ext.eventbus.ThreadsafeEventBus;

/**
 * A ForwardingIterator which is also Subscribable.
 *
 * This class posts the following events:
 *   <ul><li><b>Events.RemoveEvent&lt;SubscribableIterator, E&gt;</b> <i>Sourced to: this</i> - when an item is removed
 *           from the iterator</li></ul>
 */
public abstract class SubscribableIterator<E> extends ForwardingIterator<E> implements Subscribable {
    /* The EventBus instance we will use to communicate with others */
    @Getter private final EventBus eventBus = new ThreadsafeEventBus(getClass().getName());
    
    /** Posts our remove event */
    @Publish(Events.RemoveEvent.class)
    protected void postRemoveEvent(final E element) { eventBus.post(Events.newRemoveEvent(this, element)); }
    
    /**
     * The value we last returned for next()
     */
    private Optional<E> lastNext;
    
    @Override protected abstract Iterator<E> delegate();
    
    @Override public E next() {
        /* Store this so we can remove it later */
        lastNext = Optional.fromNullable(super.next());
        return lastNext.orNull();
    }
    
    @Override public void remove() {
        super.remove();
        if (lastNext != null) {
            /* Post the event and set our value to null */
            postRemoveEvent(lastNext.orNull());
            lastNext = null;
        }
    }
    
    @Override public SubscribableIterator<E> register(final Object o) { eventBus.register(o); return this; }
    @Override public SubscribableIterator<E> unregister(final Object o) { eventBus.unregister(o); return this; }

    /**
     * A static creator function.
     *
     * @param backingIterator the iterator that you want to be subscribable
     * @return the SubscribableIterator
     */
    @SuppressWarnings("unchecked")
    public static <E> SubscribableIterator<E> wrap(final Iterator<? extends E> backingIterator) {
        if (backingIterator instanceof SubscribableImpl) {
            return (SubscribableImpl<E>) backingIterator;
        }
        return new SubscribableImpl<>((Iterator<E>) backingIterator);
    }

    /**
     * A private implementation
     */
    private static class SubscribableImpl<E> extends SubscribableIterator<E> {
        private final Iterator<E> delegate;
        @Override protected Iterator<E> delegate() { return delegate; }
        private SubscribableImpl(final Iterator<E> delegate) { this.delegate = delegate; }
    }
}
