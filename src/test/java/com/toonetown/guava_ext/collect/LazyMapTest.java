package com.toonetown.guava_ext.collect;

import lombok.Getter;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet;

/**
 * Unit test for LazyMap
 */
public class LazyMapTest {

    private static class TestMap extends LazyMap<String, Integer> {
        private final Map<String, Integer> delegate = Maps.newHashMap();
        @Override protected Map<String, Integer> delegate() { return delegate; }
        private final ImmutableSet<String> allKeys = ImmutableSet.of("a", "b", "c");
        @Override public Set<String> allPotentialKeys() { return allKeys; }
        @Getter private int numLoads;

        @Override protected Map<String, Integer> loadAll(final Set<String> keys) {
            final Map<String, Integer> tMap = Maps.newHashMap();
            for (final String k : keys) {
                if (k.equals("a")) {
                    tMap.put("a", 1);
                } else if (k.equals("b") || k.equals("c")) {
                    tMap.put("b", 2);
                    tMap.put("c", 3);
                }
            }
            numLoads++;
            return tMap;
        }
    }

    @Test
    public void testContainsKey() {
        final TestMap map = new TestMap();
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("z"));
        assertFalse(map.containsKey("z"));
        assertEquals(map.getNumLoads(), 0);
    }
    
    @Test
    public void testContainsValue() {
        final TestMap map = new TestMap();
        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(10));
        assertEquals(map.getNumLoads(), 1);
    }
    
    @Test
    public void testEntrySet() {
        final TestMap map = new TestMap();
        assertEquals(map.entrySet().size(), 3);
        assertEquals(map.getNumLoads(), 1);
    }
    
    @Test
    public void testGet() {
        final TestMap map = new TestMap();
        assertEquals(map.get("a"), (Integer) 1);
        assertEquals(map.get("b"), (Integer) 2);
        assertEquals(map.get("c"), (Integer) 3);
        assertNull(map.get("z"));
        assertEquals(map.getNumLoads(), 2);
    }
    
    @Test
    public void testIsEmpty() {
        final TestMap map = new TestMap();
        assertFalse(map.isEmpty());
        assertEquals(map.getNumLoads(), 1);
    }
    
    @Test
    public void testKeySet() {
        final TestMap map = new TestMap();
        assertEquals(map.keySet().size(), 3);
        assertEquals(map.getNumLoads(), 0);
    }

    @Test
    public void testSize() {
        final TestMap map = new TestMap();
        assertEquals(map.size(), 3);
        assertEquals(map.getNumLoads(), 0);
    }
    
    @Test
    public void testValues() {
        final TestMap map = new TestMap();
        assertEquals(map.values().size(), 3);
        assertEquals(map.getNumLoads(), 1);
    }

    @Test
    public void testMultiActions() {
        final TestMap map = new TestMap();
        assertEquals(map.get("b"), (Integer) 2);
        assertEquals(map.getNumLoads(), 1);
        assertEquals(map.containsKey("c"), true);
        assertEquals(map.getNumLoads(), 1);
        assertEquals(map.size(), 3);
        assertEquals(map.getNumLoads(), 1);
        int count = 0;
        for (final String k : map.allPotentialKeys()) {
            assertNotNull(map.get(k));
            count++;
        }
        assertEquals(count, 3);
        assertEquals(map.getNumLoads(), 2);
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testClear() {
        new TestMap().clear();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPut() {
        new TestMap().put("y", 9);
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutAll() {
        new TestMap().putAll(Collections.singletonMap("y", 9));
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRemove() {
        new TestMap().remove("a");
    }
 

}
