package com.toonetown.guava_ext.collect;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import com.google.common.collect.Sets;
import com.google.common.collect.ForwardingMap;

/**
 * A forwarding map which lazily loads its values on-demand.  This map behaves like a Read-only map (that is, calls
 * to put/clear/remove/etc will fail).
 */
@Slf4j
public abstract class LazyMap<K, V> extends ForwardingMap<K, V> {
    
    /* Read functions - ensure that the correct stuff is loaded first */
    @Override public boolean containsKey(final Object key) {
        return allPotentialKeys().contains(key);
    }
    @Override public boolean containsValue(final Object value) {
        ensureLoaded();
        return super.containsValue(value);
    }
    @Override public Set<Map.Entry<K, V>> entrySet() {
        ensureLoaded();
        return super.entrySet();
    }
    @Override public V get(final Object key) {
        return super.get(ensureLoaded(key));
    }
    @Override public boolean isEmpty() {
        ensureLoaded();
        return super.isEmpty();
    }
    @Override public Set<K> keySet() {
        return allPotentialKeys();
    }
    @Override public int size() {
        return allPotentialKeys().size();
    }
    @Override public Collection<V> values() {
        ensureLoaded();
        return super.values();
    }
    
    /* Write functions - not allowed */
    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public V put(final K key, final V value) { throw new UnsupportedOperationException(); }
    @Override public void putAll(final Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }
    @Override public V remove(final Object object) { throw new UnsupportedOperationException(); }
    
    /**
     * Ensures that the given keys are loaded
     */
    private void ensureLoaded(final Set<?> keys) {
        /* Get the set of keys we don't have yet */
        final Set<K> keysToLoad = Sets.newLinkedHashSet();
        final Set<K> possibleKeys = allPotentialKeys();
        for (final Object key : keys) {
            if (possibleKeys.contains(key) && !(delegate().containsKey(key))) {
                try {
                    @SuppressWarnings("unchecked")
                    final K k = (K) key;
                    keysToLoad.add(k);
                } catch (ClassCastException e) {
                    LazyMap.log.info("Not loading key {}", key);
                }
            }
        }
        if (!keysToLoad.isEmpty()) {
            delegate().putAll(loadAll(keysToLoad));
        }
    }
    
    /**
     * Ensures a single key is loaded.  Returns the key itself so that it can be used in another call
     */
    private Object ensureLoaded(final Object key) {
        ensureLoaded(Collections.singleton(key));
        return key;
    }
    
    /**
     * Ensures all keys are loaded
     */
    private void ensureLoaded() {
        ensureLoaded(allPotentialKeys());
    }
    
    /**
     * Loads the given keys.  This function can load more than the given keys, if desired - and all keys will be put
     * into the map for later operations.  If a key is requested to be loaded, but is not returned, then the resulting
     * value will not exist in this map.
     *
     * @param keysToLoad the keys that should be loaded
     * @return a map of the loaded keys
     */
    protected abstract Map<K, V> loadAll(final Set<K> keysToLoad);
    
    /**
     * Returns the set of all potential keys.
     *
     * @return a set of all possible keys in this map (loaded or not)
     */
    public abstract Set<K> allPotentialKeys();
}
