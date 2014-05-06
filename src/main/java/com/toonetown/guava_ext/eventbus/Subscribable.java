package com.toonetown.guava_ext.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * An interface which indicates that a class can be subscribed to (via EventBus)
 */
public interface Subscribable {
    /**
     * Returns the EventBus instance associated with this object.  Other objects can use this to subscribe to
     * events that it posts.  In general, it is favorable to use register(Object) or unregister(Object)
     */
    EventBus getEventBus();
    
    /**
     * A fluent function for registering on-the-fly.  Implementations of this interface should return themselves.
     *
     * @param o the object to register
     * @return this object
     */
    Subscribable register(Object o);
    
    /**
     * A fluent function that will unregister on-the-fly.  Implementations of this interface should return themselves.
     *
     * @param o the object to unregister
     * @return this object
     */
    Subscribable unregister(Object o);
    
}
