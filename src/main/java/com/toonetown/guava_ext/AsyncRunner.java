package com.toonetown.guava_ext;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A class which provides running and waiting functionality for an arbitrary runnable
 */
@RequiredArgsConstructor
public class AsyncRunner {

    /** The runnable that we will actually run */
    private final Runnable runnable;

    /** An atomic reference that we can synchronize on */
    private final AtomicReference<ListenableFuture<?>> pending = new AtomicReference<>();

    /**
     * Returns whether or not this async runner is currently running.
     *
     * @return true if this runner is still in process
     */
    public boolean isRunning() {
        return (pending.get() != null);
    }

    /**
     * Runs the given runnable.  If this runner is already running something, the new runnable will NOT be run.
     *
     * @return true if the runner was started.  False if it was not.
     */
    public boolean runAsync() {
        if (!isRunning()) {
            /* Synchronize on it - and see if we *really* aren't running */
            synchronized (pending) {
                if (!isRunning()) {
                    /* Still not running - so run */
                    pending.set(Threads.executor().submit(new Runnable() {
                        @Override public void run() {
                            runnable.run();
                            pending.set(null);
                        }
                    }));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Waits until our function completes.  You can call this function even if it is not running, and it will
     * return immediately.
     */
    public void waitForCompletion() {
        final ListenableFuture<?> future = pending.get();
        if (future != null) {
            Futures.getUnchecked(future);
        }
    }

    /**
     * Runs the given runnable, and if it is started, then it will wait until completion
     */
    public void runAndWait() {
        if (runAsync()) {
            waitForCompletion();
        }
    }

}
