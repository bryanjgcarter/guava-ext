package com.toonetown.guava_ext;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ForwardingMap;

/**
 * A class which will look up enums in a map.  It also is an unmodifiable map of enum value to key, so you can iterate
 * over them if you like.
 *
 * Really cool stuff can happen ("smart enums") if you do something similar to the following *within* your enum
 * itself:
 *      public enum MyEnum implements EnumLookup.Keyed<Integer> {
 *          ...
 *          private static final EnumLookup<MyEnum, Integer> $ALL = EnumLookup.of(MyEnum.class);
 *          public static MyEnum find(final Integer i) throws NotFoundException { return $ALL.find(i); }
 *          public static Set<MyEnum> all() { return $ALL.keySet(); }
 *      }
 * You are then able to call static functions MyEnum.find() and MyEnum.all()
 */
public class EnumLookup<K extends Enum<K> & EnumLookup.Findable, V> extends ForwardingMap<K, V> {
    
    /** A shared interface that both Keyed and MultiKeyed extend for type safety.  Do not use directly */
    public interface Findable { }
    
    /**
     * An enum can implement this interface and be keyed via an EnumLookup
     */
    public interface Keyed<V> extends Findable {
        /** 
         * Returns the value for this enum.
         */
        V getValue();
    }
    
    /**
     * An enum can implement this interface multiple times and be keyed via an EnumLookup
     */
    public interface MultiKeyed extends Findable {
        /**
         * Returns the array of key values for this enum.  Any order is fine - the index used to read from this
         * array is passed in to the on() functions.  The actual value at the supplied index will be cast to the
         * correct generic
         *
         * @return the values for this enum
         */
        Object[] getValue();
    }
    

    /** Our backing BiMap */
    private final BiMap<K, V> delegate;
    @Override protected BiMap<K, V> delegate() { return delegate; }
    
    /** A BiMap that we use to look up the values */
    private final BiMap<V, K> inverse;
    
    /** If we are a string, this determines our case-sensitivity */
    private final boolean caseSensitive;
    
    /** Returns the value to work off of */
    @SuppressWarnings("unchecked")
    private V keyForValue(final V value) {
        
        if (value instanceof String && !caseSensitive) {
            return (V) ((String) value).toLowerCase();
        }
        return value;
    }
    
    /** Extracts the key value from a findable object */
    @SuppressWarnings("unchecked")
    private V extractKeyValue(final Findable findable, final int idx) {
        if (findable instanceof MultiKeyed) {
            return (V) ((MultiKeyed) findable).getValue()[idx];
        } else {
            return ((Keyed<V>) findable).getValue();
        }
    }
    
    /** A private constructor */
    private EnumLookup(final Class<K> enumClass, final int idx, final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        final BiMap<K, V> biMap = EnumHashBiMap.create(enumClass);
        for (final K k : enumClass.getEnumConstants()) {
            biMap.put(k, keyForValue(extractKeyValue(k, idx)));
        }
        this.delegate = Maps.unmodifiableBiMap(biMap);
        this.inverse = delegate.inverse();
    }
    
    /**
     * Finds the given ID and returns the corresponding enum value
     *
     * @param id the id to look up
     * @return the corresponding enum value
     * @throws NotFoundException if the ID doesn't exist in the enum
     */
    public K find(final V id) throws NotFoundException {
        final V keyValue = keyForValue(id);
        if (inverse.containsKey(keyValue)) {
            return inverse.get(keyValue);
        }
        throw new NotFoundException("Enum with value " + id + " not found");
    }
    
    /**
     * Finds the given ID and returns the corresponding enum value without throwing an exception
     *
     * @param id the id to look up
     * @param defaultValue the value to use if the id is not found
     */
    public K find(final V id, final K defaultValue) {
        try {
            return find(id);
        } catch (NotFoundException e) {
            return defaultValue;
        }
    }
    
    /**
     * Creates an EnumLookup instance for the enum of the given class.  If this is a Keyed<String> enum, the finding
     * will happen case-insensitively.
     *
     * @param enumClass the class of the enum to create the lookup for
     * @return an instance of EnumLookup
     */
    public static <K extends Enum<K> & Keyed<V>, V> EnumLookup<K, V> of(final Class<K> enumClass) {
        return new EnumLookup<>(enumClass, -1, false);
    }

    /**
     * Creates an EnumLookup instance for the enum of the given class.  This version will do case sensitive checking
     * based off of the given value.
     *
     * @param enumClass the class of the enum to create the lookup for
     * @param caseSensitive true for case sensitive checking
     * @return an instance of EnumLookup
     */
    public static <K extends Enum<K> & Keyed<String>> EnumLookup<K, String> of(final Class<K> enumClass,
                                                                               final boolean caseSensitive) {
        return new EnumLookup<>(enumClass, -1, caseSensitive);
    }

    /**
     * Creates an EnumLookup instance for the multi-keyed enum of the given class.  If this is a MultiKeyed<String>
     * enum, the finding will happen case-insensitively.
     *
     * @param enumClass the class of the enum to create the lookup for
     * @param idx the index within getValue() to use for this key
     * @return an instance of EnumLookup
     */
    public static <K extends Enum<K> & MultiKeyed, V> EnumLookup<K, V> of(final Class<K> enumClass, final int idx) {
        return new EnumLookup<>(enumClass, idx, false);
    }

    /**
     * Creates an EnumLookup instance for the multi-keyed enum of the given class.  If this is a MultiKeyed<String>
     * enum, the finding will happen case-insensitively.  This version will do case sensitive checking based off of the
     * indexed value.
     *
     * @param enumClass the class of the enum to create the lookup for
     * @param idx the index within getValue() to use for this key
     * @param caseSensitive true for case sensitive checking
     * @return an instance of EnumLookup
     */
    public static <K extends Enum<K> & MultiKeyed> EnumLookup<K, String> of(final Class<K> enumClass,
                                                                            final int idx,
                                                                            final boolean caseSensitive) {
        return new EnumLookup<>(enumClass, idx, caseSensitive);
    }

}
