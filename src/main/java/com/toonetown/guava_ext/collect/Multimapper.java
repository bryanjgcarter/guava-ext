package com.toonetown.guava_ext.collect;

import lombok.Data;

import java.util.Iterator;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A which adds to the standard guava Multimaps class.  This includes functions such as creating multimaps from
 * regular maps, etc.
 */
public class Multimapper {
    private Multimapper() {}

    /**
     * An implementation of Map.Entry class which always has iterable values
     */
    @Data private static class Entry<K, V> implements Map.Entry<K, Iterable<? extends V>> {
        /** The key for our entry */
        private final K key;
        /** The values for our entry */
        private final Iterable<? extends V> value;

        @Override public Iterable<? extends V> setValue(final Iterable<? extends V> inValue) {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * A class which can easily be iterated over for map entries
     */
    @Data private static class EntryIterable<K, V> implements Iterable<Entry<K, V>> {
        /** The base iterator we are transforming */
        private final Iterator<? extends Map.Entry<? extends K, ? extends Iterable<? extends V>>> source;
        
        @Override public Iterator<Entry<K, V>> iterator() {
            return Iterators.transform(source,
                new Function<Map.Entry<? extends K, ? extends Iterable<? extends V>>, Entry<K, V>>() {
                    @Override public Entry<K, V> apply(final Map.Entry<? extends K,
                                                                       ? extends Iterable<? extends V>> e) {
                        return new Entry<>(e.getKey(), e.getValue());
                    }
                });
        }
        
        /**
         * Returns an EntryIterable for the given map with iterable values
         *
         * @param map the map to get an EntryIterable from
         */
        public static <K, V> EntryIterable<K, V> forIterableMap(final Map<? extends K,
                                                                          ? extends Iterable<? extends V>> map) {
            return new EntryIterable<>(map.entrySet().iterator());
        }

        /**
         * Returns an EntryIterable for the given map with singleton values
         *
         * @param map the map to get an EntryIterable from
         */
        public static <K, V> EntryIterable<K, V> forSingletonMap(final Map<? extends K, ? extends V> map) {
            final Iterator<? extends Map.Entry<? extends K, ? extends V>> base = map.entrySet().iterator();
            return new EntryIterable<>(new AbstractIterator<Map.Entry<? extends K, Iterable<? extends V>>>() {
                @Override protected Map.Entry<? extends K, Iterable<? extends V>> computeNext() {
                    if (base.hasNext()) {
                        final Map.Entry<? extends K, ? extends V> e = base.next();
                        return new Entry<>(e.getKey(), Collections.singletonList(e.getValue()));
                    }
                    return endOfData();
                }
            });
        }
    }
    
    /**
     * Stores all the values from the given entries into the multimap.
     *
     * @param multimap the target multimap
     * @param entries the values to put into the multimap
     * @return true if the multimap changed
     */
    private static <K, V> boolean putAll(final Multimap<K, V> multimap,
                                        final EntryIterable<? extends K, ? extends V> entries) {
        boolean changed = false;
        for (final Entry<? extends K, ? extends V> e : entries) {
            changed |= multimap.putAll(e.getKey(), e.getValue());
        }
        return changed;
    }
    
    /**
     * Stores all the values (as collections) from the given valueMap into the multimap.
     *
     * @param multimap the target multimap
     * @param valueMap the values to put into the multimap
     * @return true if the multimap changed
     */
    public static <K, V> boolean putAllIterables(final Multimap<K, V> multimap,
                                                 final Map<? extends K, ? extends Iterable<? extends V>> valueMap) {
        return putAll(multimap, EntryIterable.forIterableMap(valueMap));
    }

    /**
     * Stores all the values (as single items) from the given valueMap into the multimap.
     *
     * @param multimap the target multimap
     * @param valueMap the values to put into the multimap
     * @return true if the multimap changed
     */
    public static <K, V> boolean putAllSingletons(final Multimap<K, V> multimap,
                                                  final Map<? extends K, ? extends V> valueMap) {
        return putAll(multimap, EntryIterable.forSingletonMap(valueMap));
    }

    /**
     * Puts all the values (as collections) from the given valueMap into the multimap, replacing any existing values
     * for that key.
     *
     * @param multimap the target multimap
     * @param entries the values to replace into the multimap
     * @return the replaced values, or an empty collection if no values were previously associated with the key.
     */
    private static <K, V> Multimap<K, V> replaceAll(final Multimap<K, V> multimap,
                                                    final EntryIterable<? extends K, ? extends V> entries) {
        final Multimap<K, V> ret = ArrayListMultimap.create();
        for (final Entry<? extends K, ? extends V> e : entries) {
            if (ret.containsKey(e.getKey())) {
                /* We have already replaced it - so just put it now */
                multimap.putAll(e.getKey(), e.getValue());
            } else {
                /* We haven't replaced it yet - so do that now */
                ret.putAll(e.getKey(), multimap.replaceValues(e.getKey(), e.getValue()));
            }
        }
        return ret;
    }

    /**
     * Stores all the values (as collections) from the given valueMap into the multimap.
     *
     * @param multimap the target multimap
     * @param valueMap the values to put into the multimap
     * @return true if the multimap changed
     */
    public static <K, V> Multimap<K, V> replaceAllIterables(final Multimap<K, V> multimap,
                                                            final Map<? extends K,
                                                                      ? extends Iterable<? extends V>> valueMap) {
        return replaceAll(multimap, EntryIterable.forIterableMap(valueMap));
    }
    
    /**
     * Stores all the values (as single items) from the given valueMap into the multimap.
     *
     * @param multimap the target multimap
     * @param valueMap the values to put into the multimap
     * @return true if the multimap changed
     */
    public static <K, V> Multimap<K, V> replaceAllSingletons(final Multimap<K, V> multimap,
                                                             final Map<? extends K, ? extends V> valueMap) {
        return replaceAll(multimap, EntryIterable.forSingletonMap(valueMap));
    }    
}
