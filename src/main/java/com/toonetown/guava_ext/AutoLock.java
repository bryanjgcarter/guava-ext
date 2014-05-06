package com.toonetown.guava_ext;

import java.util.concurrent.locks.ReentrantLock;

/**
 * An automatically closeable class lock/unlocks a Reentrant lock automatically.  Use this by doing:
 * try (final AutoLock l = new AutoLock(...)) { ... }
 */
public class AutoLock implements AutoCloseable {
    /** The lock we wrap */
    transient private final ReentrantLock lock;
    
    /**
     * Our constructor
     */
    public AutoLock(final ReentrantLock lock) {
        this.lock = lock;
        this.lock.lock();
    }

    /**
     * The close function which unlocks our lock
     */
    @Override public void close() {
        this.lock.unlock();
    }
}
