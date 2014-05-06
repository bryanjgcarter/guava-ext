package com.toonetown.guava_ext;

import com.google.common.cache.CacheBuilder;

/**
 * Utilities for dealing with caches (and builders)
 */
public class Caches {
    private Caches() { }

    /**
     * Helper function which uses parameterization to cast a builder.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> CacheBuilder<K, V> castBuilder(final CacheBuilder<?, ?> cb) {
        return (CacheBuilder<K, V>) cb;
    }

    /**
     * Helper function to create a new cache using parameterization
     */
    public static <K, V> CacheBuilder<K, V> newBuilder() {
        return Caches.castBuilder(CacheBuilder.newBuilder());
    }

}
