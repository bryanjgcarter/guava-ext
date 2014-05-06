package com.toonetown.guava_ext.eventbus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

import org.slf4j.LoggerFactory;

/**
 * A static class which contains some common event classes (around modification and exception handling).  These event 
 * classes can be reused as-is or subclassed.
 */
public class Events {
    private Events() {}
    
    /**
     * Returns a new AddEvent instance
     */
    public static <S extends Subscribable, E> AddEvent<S, E> newAddEvent(final S source,
                                                                         final E element) {
        return new AddEvent<>(source, element);
    }
    
    /**
     * Returns a new RemoveEvent instance
     */
    public static <S extends Subscribable, E> RemoveEvent<S, E> newRemoveEvent(final S source,
                                                                               final E element) {
        return new RemoveEvent<>(source, element);
    }
    
    /**
     * Returns a new ModifyEvent instance
     */
    public static <S extends Subscribable, E> ModifyEvent<S, E> newModifyEvent(final S source,
                                                                               final E element,
                                                                               final E oldValue) {
        return new ModifyEvent<>(source, element, oldValue);
    }
    
    /**
     * Returns a new ModifyEvent instance
     */
    public static <S extends Subscribable, X extends Throwable>
    ExceptionEvent<S, X> newExceptionEvent(final S source, final X exception) {
        return new ExceptionEvent<>(source, exception);
    }
    
    /**
     * A base event which contains a source.  Most Subscribables should post subclasses of this event.
     */
    public static abstract class Base<S extends Subscribable> {
        /** The source which caused this event */
        public abstract S source();
    }
    
    /**
     * An interface which indicates an event that can regenerate itself with a new source.
     */
    public interface Repostable<T extends Base<N>, N extends Subscribable> {
        /** Creates a new event that is identical to this one with a different source */
        T newSource(final N newSource);
    }
    
    /**
     * An enum which contains types of change events
     */
    public static enum ChangeType {
        /** Change is an addition */
        ADDITION,
        /** Change is a removal */
        REMOVAL,
        /** Change is a modification */
        MODIFICATION
    }
    
    /**
     * A change event (add/remove/modify)
     */
    public static abstract class ChangeEvent<S extends Subscribable, E> extends Base<S>
    implements Repostable<ChangeEvent<Subscribable, E>, Subscribable> {
        /** The type of change event */
        public abstract ChangeType type();
        /** The element that was changed */
        public abstract E element();
    }
    
    /**
     * An addition event
     */
    @Data @Accessors(fluent = true) @EqualsAndHashCode(callSuper = false)
    public static class AddEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.ADDITION;
        private final S source;
        private final E element;
        @Override public AddEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.newAddEvent(newSource, element);
        }
    }
    
    /**
     * A removal event
     */
    @Data @Accessors(fluent = true) @EqualsAndHashCode(callSuper = false)
    public static class RemoveEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.REMOVAL;
        private final S source;
        private final E element;
        @Override public RemoveEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.newRemoveEvent(newSource, element);
        }
    }
    
    /**
     * A modification event
     */
    @Data @Accessors(fluent = true) @EqualsAndHashCode(callSuper = false)
    public static class ModifyEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.MODIFICATION;
        private final S source;
        private final E element;
        private final E oldValue;
        @Override public ModifyEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.newModifyEvent(newSource, element, oldValue);
        }
    }
    
    /**
     * An exception event
     */
    @Data @Accessors(fluent = true) @EqualsAndHashCode(callSuper = false)
    public static class ExceptionEvent<S extends Subscribable, X extends Throwable> extends Base<S>
    implements Repostable<ExceptionEvent<Subscribable, X>, Subscribable> {
        private final S source;
        private final X exception;
        @Override public ExceptionEvent<Subscribable, X> newSource(final Subscribable newSource) {
            return Events.newExceptionEvent(newSource, exception);
        }
    }

    /**
     * A handler which will log dead exception events to the given logger
     */
    @RequiredArgsConstructor
    public static class UnhandledExceptionLogger {
        @Subscribe public void onDeadEvent(final DeadEvent event) {
            if (event.getEvent() instanceof ExceptionEvent) {
                final ExceptionEvent e = (ExceptionEvent) event.getEvent();
                LoggerFactory.getLogger(e.source().getClass()).error("Unhandled ExceptionEvent on {}",
                                                                     e.source(),
                                                                     e.exception());
            }
        }
    }
}
