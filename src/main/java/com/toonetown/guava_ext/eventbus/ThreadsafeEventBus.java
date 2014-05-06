package com.toonetown.guava_ext.eventbus;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;

/**
 * Extends EventBus to be threadsafe by queueing register and subscribe events that happen during posting
 */
public class ThreadsafeEventBus extends EventBus {
    /** An enum of possible actions */
    private enum Actions { POST, REGISTER, UNREGISTER, END }

    /** A flag that checks if an action is currently in-progress */
    private final AtomicBoolean actionInProgress = new AtomicBoolean();

    /** The outstanding queue that gets flushed */
    private final Queue<Action> actionQueue = Queues.newConcurrentLinkedQueue();

    public ThreadsafeEventBus(final String name) {
        super(name);
    }

    /** Flushes the queue */
    private void flushQueue() {
        while (true) {
            final Action act = Objects.firstNonNull(actionQueue.poll(), new Action(Actions.END, null));
            switch (act.action) {
                case POST:
                    post(act.object);
                    break;
                case REGISTER:
                    register(act.object);
                    break;
                case UNREGISTER:
                    unregister(act.object);
                    break;
                case END:
                default:
                    return;
            }
        }
    }

    @Override public synchronized void post(final Object event) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.POST, event));
        } else {
            super.post(event);
            actionInProgress.set(false);
            flushQueue();
        }
    }

    @Override public synchronized void register(final Object object) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.REGISTER, object));
        } else {
            super.register(object);
            actionInProgress.set(false);
            flushQueue();
        }
    }

    @Override public synchronized void unregister(final Object object) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.UNREGISTER, object));
        } else {
            super.unregister(object);
            actionInProgress.set(false);
            flushQueue();
        }
    }

    /** An object to track queued actions */
    private static class Action {
        private final Actions action;
        private final Object object;
        private Action(final Actions action, final Object object) {
            this.action = action;
            this.object = object;
        }
    }
}
