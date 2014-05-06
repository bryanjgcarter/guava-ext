package com.toonetown.guava_ext.collect;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Map;
import java.util.Arrays;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

/**
 * Unit test for Multimapper
 */
public class MultimapperTest {

    private static Map<String, Iterable<Integer>> iterableMap_init() {
        final Map<String, Iterable<Integer>> map = Maps.newHashMap();
        map.put("a", Arrays.asList(10, 9));
        map.put("z", Arrays.asList(8));
        return map;
    }

    private static Map<String, Iterable<Integer>> iterableMap() {
        final Map<String, Iterable<Integer>> map = Maps.newHashMap();
        map.put("a", Arrays.asList(1, 2, 9));
        map.put("b", Arrays.asList(4));
        return map;
    }

    private static Map<String, Integer> singletonMap_init() {
        final Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 10);
        map.put("z", 9);
        return map;
    }
    
    private static Map<String, Integer> singletonMap() {
        final Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("b", 2);
        return map;
    }
    
    @Test
    public void testPutAllIterables() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllIterables(map, iterableMap_init()));
        assertEquals(map.size(), 3);
        assertEquals(map.get("a").size(), 2);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testPutAllSingletons() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllSingletons(map, singletonMap_init()));
        assertEquals(map.size(), 2);
        assertEquals(map.get("a").size(), 1);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }

    @Test
    public void testReplaceAllIterables() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap_init()).size(), 0);
        assertEquals(map.size(), 3);
        assertEquals(map.get("a").size(), 2);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }

    @Test
    public void testReplaceAllSingletons() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap_init()).size(), 0);
        assertEquals(map.size(), 2);
        assertEquals(map.get("a").size(), 1);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }

    @Test
    public void testPutAllIterables_existing() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllIterables(map, iterableMap_init()));
        assertTrue(Multimapper.putAllIterables(map, iterableMap()));
        assertEquals(map.size(), 7);
        assertEquals(map.get("a").size(), 5);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testPutAllSingletons_existing() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllSingletons(map, singletonMap_init()));
        assertTrue(Multimapper.putAllSingletons(map, singletonMap()));
        assertEquals(map.size(), 4);
        assertEquals(map.get("a").size(), 2);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testReplaceAllIterables_existing() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap()).size(), 2);
        assertEquals(map.size(), 5);
        assertEquals(map.get("a").size(), 3);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testReplaceAllSingletons_existing() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap()).size(), 1);
        assertEquals(map.size(), 3);
        assertEquals(map.get("a").size(), 1);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testPutAllIterables_same() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllIterables(map, iterableMap_init()));
        assertTrue(Multimapper.putAllIterables(map, iterableMap_init()));
        assertEquals(map.size(), 6);
        assertEquals(map.get("a").size(), 4);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 2);
    }
    
    @Test
    public void testPutAllSingletons_same() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertTrue(Multimapper.putAllSingletons(map, singletonMap_init()));
        assertTrue(Multimapper.putAllSingletons(map, singletonMap_init()));
        assertEquals(map.size(), 4);
        assertEquals(map.get("a").size(), 2);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 2);
    }
    
    @Test
    public void testReplaceAllIterables_same() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap_init()).size(), 3);
        assertEquals(map.size(), 3);
        assertEquals(map.get("a").size(), 2);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testReplaceAllSingletons_same() {
        final Multimap<String, Integer> map = ArrayListMultimap.create();
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap_init()).size(), 2);
        assertEquals(map.size(), 2);
        assertEquals(map.get("a").size(), 1);
        assertFalse(map.containsKey("b"));
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testPutAllIterables_hash() {
        final Multimap<String, Integer> map = HashMultimap.create();
        assertTrue(Multimapper.putAllIterables(map, iterableMap_init()));
        assertTrue(Multimapper.putAllIterables(map, iterableMap()));
        assertEquals(map.size(), 6);
        assertEquals(map.get("a").size(), 4);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testPutAllSingletons_hash() {
        final Multimap<String, Integer> map = HashMultimap.create();
        assertTrue(Multimapper.putAllSingletons(map, singletonMap_init()));
        assertTrue(Multimapper.putAllSingletons(map, singletonMap()));
        assertEquals(map.size(), 4);
        assertEquals(map.get("a").size(), 2);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testReplaceAllIterables_hash() {
        final Multimap<String, Integer> map = HashMultimap.create();
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllIterables(map, iterableMap()).size(), 2);
        assertEquals(map.size(), 5);
        assertEquals(map.get("a").size(), 3);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    
    @Test
    public void testReplaceAllSingletons_hash() {
        final Multimap<String, Integer> map = HashMultimap.create();
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap_init()).size(), 0);
        assertEquals(Multimapper.replaceAllSingletons(map, singletonMap()).size(), 1);
        assertEquals(map.size(), 3);
        assertEquals(map.get("a").size(), 1);
        assertEquals(map.get("b").size(), 1);
        assertEquals(map.get("z").size(), 1);
    }
    


}
