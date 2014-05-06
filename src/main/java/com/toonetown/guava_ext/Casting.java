package com.toonetown.guava_ext;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Utility functions for casting values, or returning null if they do not match
 */
public class Casting {
    private Casting() { }

    /**
     * Casts an object to the given class.  Returns as an optional
     *
     * @param obj The object to cast
     * @param asClass the class to cast as
     * @return an optional value cast, or an absent value if the object is not of the correct type
     */
    public static <T> Optional<T> cast(@Nullable final Object obj, @Nullable final Class<T> asClass) {
        if (asClass != null && asClass.isInstance(obj)) {
            return Optional.of(asClass.cast(obj));
        }
        return Optional.absent();
    }

    /**
     * Casts an optional to the given class.  Returns an absent optional if the original is absent.
     *
     * @param obj The object to cast
     * @param asClass the class to cast as
     * @return an optional value cast, or an absent value if the object is not of the correct type
     */
    public static <T> Optional<T> cast(final Optional<?> obj, @Nullable final Class<T> asClass) {
        return Casting.cast(obj.orNull(), asClass);
    }

    /**
     * Casts the given class to be the same type as asClass
     *
     * @param cls the class to cast
     * @param asClass the class to cast as
     * @return an optional class cast, or an absent value if the given class is not assignable
     */
    public static <T> Optional<Class<? extends T>> subclass(@Nullable final Class<?> cls,
                                                            @Nullable final Class<T> asClass) {
        if (cls != null && asClass != null && asClass.isAssignableFrom(cls)) {
            return Optional.<Class<? extends T>>of(cls.asSubclass(asClass));
        }
        return Optional.absent();
    }

    /**
     * Casts the given iterable to have the same type as asClass.  This just calls Iterables.filter but exists to
     * provide a common interface for casting.
     *
     * @param iterable the iterable to cast
     * @param asClass the class to cast as
     * @return an iterable with only the values which are instances of the class remaining
     */
    public static <T> Iterable<T> cast(@Nullable final Iterable<?> iterable, @Nullable final Class<T> asClass) {
        if (iterable == null || asClass == null) {
            return ImmutableSet.of();
        }
        return Iterables.filter(iterable, asClass);
    }

    /**
     * Casts the given iterable of classes to be the same type as asClass.
     *
     * @param iterable the iterable to cast
     * @param asClass the class to cast as
     * @return an iterable with only the values which are instances of the class remaining
     */
    public static <T> Iterable<Class<? extends T>> subclass(@Nullable final Iterable<?> iterable,
                                                            @Nullable final Class<T> asClass) {
        if (iterable == null || asClass == null) {
            return ImmutableSet.of();
        }
        return new FluentIterable<Class<? extends T>>() {
            @Override public Iterator<Class<? extends T>> iterator() {
                return Casting.subclass(iterable.iterator(), asClass);
            }
        };
    }

    /**
     * Casts the given iterator to have the same type as asClass.  This just calls Iterators.filter but exists
     * to provide a common interface for casting.
     *
     * @param iterator the iterator to cast
     * @param asClass the class to cast as
     * @return an iterator with only the values which are instances of the class remaining
     */
    public static <T> Iterator<T> cast(@Nullable final Iterator<?> iterator, @Nullable final Class<T> asClass) {
        if (iterator == null || asClass == null) {
            return Iterators.emptyIterator();
        }
        return Iterators.filter(iterator, asClass);
    }

    /**
     * Casts the given iterator of classes to be the same type as asClass.
     *
     * @param iterator the iterator to cast
     * @param asClass the class to cast as
     * @return an iterator with only the values which are instances of the class remaining
     */
    @SuppressWarnings("unchecked") /* We can cast, because we remove all non-matching */
    public static <T> Iterator<Class<? extends T>> subclass(@Nullable final Iterator<?> iterator,
                                                            @Nullable final Class<T> asClass) {
        if (iterator == null || asClass == null) {
            return Iterators.emptyIterator();
        }
        return (Iterator<Class<? extends T>>) Iterators.filter(iterator, new Predicate<Object>() {
            @Override public boolean apply(@Nullable final Object input) {
                return Casting.subclass(Casting.cast(input, Class.class).orNull(), asClass).isPresent();
            }
        });
    }

}
