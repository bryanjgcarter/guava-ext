package com.toonetown.guava_ext.collect;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.toonetown.guava_ext.testing.DataProviders;
import com.toonetown.guava_ext.eventbus.Events;
import static com.toonetown.guava_ext.testing.DataProviders.concat;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit test for SubscribableSet
 */
@Slf4j
public class SubscribableSetTest {
    /**
     * A static class which counts events
     */
    public static class CountingListener<T> {
        @Getter private final SubscribableSet<T> set;
        public CountingListener(final SubscribableSet<T> set) {
            this.set = set.register(this);
        }
        
        private Set<T> changes = Sets.newHashSet();
        private Set<T> adds = Sets.newHashSet();
        private Set<T> removes = Sets.newHashSet();
        @Subscribe public void onChangeEvent(final Events.ChangeEvent<SubscribableSet<T>, T> event) {
            changes.add(event.element());
        }
        @Subscribe public void onAddEvent(final Events.AddEvent<SubscribableSet<T>, T> event) {
            adds.add(event.element());
        }
        @Subscribe public void onRemoveEvent(final Events.RemoveEvent<SubscribableSet<T>, T> event) {
            removes.add(event.element());
        }
        
        public void assertExpected(final Set<T> expectedAdds, final Set<T> expectedRemoves) {
            assertEquals(this.changes, Sets.union(expectedAdds, expectedRemoves));
            assertEquals(this.adds, expectedAdds);
            assertEquals(this.removes, expectedRemoves);
        }
    }

    private static Set<String> createBackingSet(final boolean linked) {
        return linked ? Sets.newLinkedHashSet(createBackingSet(false)) : Sets.newHashSet("a", "b", "c");
    }

    private static Set<String> createForwardingBackingSet(final boolean linked) {
        return new ForwardingSet<String>() {
            private final Set<String> delegate = createBackingSet(linked);
            @Override protected Set<String> delegate() { return delegate; }
        };
    }

    public static DataProviders.TestSet getSetData() {
        return tests(params(new CountingListener<String>(SubscribableSet.wrap(createBackingSet(true)))),
                     params(new CountingListener<String>(SubscribableSet.wrap(createBackingSet(false)))),
                     params(new CountingListener<String>(SubscribableSet.wrap(createForwardingBackingSet(true)))),
                     params(new CountingListener<String>(SubscribableSet.wrap(createForwardingBackingSet(false)))));
    }

    public static DataProviders.TestSet getSafeSetData() {
        return tests(params(new CountingListener<String>(SubscribableSet.wrapSafe(createBackingSet(true)))),
                     params(new CountingListener<String>(SubscribableSet.wrapSafe(createBackingSet(false)))),
                     params(new CountingListener<String>(SubscribableSet.wrapSafe(createForwardingBackingSet(true)))),
                     params(new CountingListener<String>(SubscribableSet.wrapSafe(createForwardingBackingSet(false)))));
    }

    public static DataProviders.TestSet getAllSetData() {
        return concat(getSetData(), getSafeSetData());
    }

    @DataProvider(name = "setData", parallel = true)
    public Object[][] setData() { return getSetData().create(); }

    @DataProvider(name = "safeSetData", parallel = true)
    public Object[][] safeSetData() { return getSafeSetData().create(); }

    @DataProvider(name = "allSetData", parallel = true)
    public Object[][] createAllSetData() { return getAllSetData().create(); }

    private static final Set<String> EMPTY_SET = Collections.emptySet();
    
    @Test(dataProvider = "allSetData")
    public void testAdd(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.add("d");
        listener.assertExpected(Sets.newHashSet("d"), EMPTY_SET);
        assertEquals(set, Sets.newHashSet("a", "b", "c", "d"));
    }

    @Test(dataProvider = "allSetData")
    public void testAdd_existing(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.add("c");
        listener.assertExpected(EMPTY_SET, EMPTY_SET);
        assertEquals(set, Sets.newHashSet("a", "b", "c"));
    }

    @Test(dataProvider = "allSetData")
    public void testAddAll(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.addAll(Sets.newHashSet("d", "e"));
        listener.assertExpected(Sets.newHashSet("d", "e"), EMPTY_SET);
        assertEquals(set, Sets.newHashSet("a", "b", "c", "d", "e"));
    }

    @Test(dataProvider = "allSetData")
    public void testAddAll_existing(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.addAll(Sets.newHashSet("c", "d"));
        listener.assertExpected(Sets.newHashSet("d"), EMPTY_SET);
        assertEquals(set, Sets.newHashSet("a", "b", "c", "d"));
    }

    @Test(dataProvider = "allSetData")
    public void testClear(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.clear();
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("a", "b", "c"));
        assertEquals(set, EMPTY_SET);
        assertEquals(set.size(), 0);
    }

    @Test(dataProvider = "allSetData")
    public void testRemove(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.remove("b");
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("b"));
        assertEquals(set, Sets.newHashSet("a", "c"));
    }

    @Test(dataProvider = "allSetData")
    public void testRemove_nonexist(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.remove("d");
        listener.assertExpected(EMPTY_SET, EMPTY_SET);
        assertEquals(set, Sets.newHashSet("a", "b", "c"));
    }

    @Test(dataProvider = "allSetData")
    public void testRemoveAll(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.removeAll(Sets.newHashSet("a", "c"));
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("a", "c"));
        assertEquals(set, Sets.newHashSet("b"));
    }
    
    @Test(dataProvider = "allSetData")
    public void testRemoveAll_nonexist(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.removeAll(Sets.newHashSet("b", "d"));
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("b"));
        assertEquals(set, Sets.newHashSet("a", "c"));
    }

    @Test(dataProvider = "allSetData")
    public void testRetainAll(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.retainAll(Sets.newHashSet("a", "c"));
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("b"));
        assertEquals(set, Sets.newHashSet("a", "c"));
    }
    
    @Test(dataProvider = "allSetData")
    public void testRetainAll_nonexist(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        set.retainAll(Sets.newHashSet("b", "d"));
        listener.assertExpected(EMPTY_SET, Sets.newHashSet("a", "c"));
        assertEquals(set, Sets.newHashSet("b"));
    }

    @Test(dataProvider = "allSetData")
    public void testCombined(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        final Set<String> newSet = Sets.newHashSet("a", "c", "d");
        set.retainAll(newSet);
        set.addAll(newSet);
        listener.assertExpected(Sets.newHashSet("d"), Sets.newHashSet("b"));
        assertEquals(set, newSet);
    }

    @Test(dataProvider = "setData", expectedExceptions = { ConcurrentModificationException.class })
    public void testConcurrent_unsafe(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        final Iterator<String> iter = set.iterator();
        set.clear();
        set.add("d");
        listener.assertExpected(Sets.newHashSet("d"), Sets.newHashSet("a", "b", "c"));
        assertEquals(set, Sets.newHashSet("d"));
        
        /* This will fail */
        Sets.newHashSet(iter);
    }

    @Test(dataProvider = "safeSetData")
    public void testConcurrent_safe(final CountingListener<String> listener) {
        final Set<String> set = listener.getSet();
        Iterator<String> iter = set.iterator();
        set.clear();
        set.add("d");
        listener.assertExpected(Sets.newHashSet("d"), Sets.newHashSet("a", "b", "c"));
        assertEquals(set, Sets.newHashSet("d"));
        
        /* Check we still have our original */
        assertEquals(Sets.newHashSet(iter), Sets.newHashSet("a", "b", "c"));
        
        /* Getting the iterator again gives us the new data */
        iter = set.iterator();
        assertEquals(Sets.newHashSet(iter), Sets.newHashSet("d"));
    }

    private Set<String> createSlowBackingSet() {
        final Set<String> backing = Sets.newHashSet("a", "b", "c");
        return new ForwardingSet<String>() {
            @Override protected Set<String> delegate() { return backing; }
            @Override public Iterator<String> iterator() {
                return Iterators.transform(super.iterator(), new Function<String, String>() {
                    @Override public String apply(final String s) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            SubscribableSetTest.log.error("Error sleeping", e);
                        }
                        return s;
                    }
                });
            }
        };
    }

    private void runInThreads(final Runnable runnable, final int numThreads) {
        final ExecutorService svc = Executors.newFixedThreadPool(numThreads);
        final List<Future<?>> futures = Lists.newArrayList();
        for (int i = 0; i < numThreads; i++) {
            futures.add(svc.submit(runnable));
        }
        for (final Future<?> future : futures) {
            try {
                Futures.getUnchecked(future);
            } catch (UncheckedExecutionException e) {
                throw ((RuntimeException) e.getCause());
            }
        }
    }

    @Test(expectedExceptions = { ConcurrentModificationException.class })
    public void testConcurrent_unsafeClear() {
        final SubscribableSet<String> set = SubscribableSet.wrap(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardClear(); }
        }, 20);
    }

    @Test(expectedExceptions = { ConcurrentModificationException.class })
    public void testConcurrent_unsafeRemove() {
        final SubscribableSet<String> set = SubscribableSet.wrap(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRemove("a"); }
        }, 20);
    }

    @Test(expectedExceptions = { ConcurrentModificationException.class })
    public void testConcurrent_unsafeRemoveAll() {
        final SubscribableSet<String> set = SubscribableSet.wrap(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRemoveAll(Sets.newHashSet("a", "c")); }
        }, 20);
    }

    @Test(expectedExceptions = { ConcurrentModificationException.class })
    public void testConcurrent_unsafeRetainAll() {
        final SubscribableSet<String> set = SubscribableSet.wrap(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRetainAll(Sets.newHashSet("a", "d")); }
        }, 20);
    }

    @Test
    public void testConcurrent_safeClear() {
        final SubscribableSet<String> set = SubscribableSet.wrapSafe(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardClear(); }
        }, 20);
        assertEquals(set, Sets.newHashSet());
    }

    @Test
    public void testConcurrent_safeRemove() {
        final SubscribableSet<String> set = SubscribableSet.wrapSafe(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRemove("a"); }
        }, 20);
        assertEquals(set, Sets.newHashSet("b", "c"));
    }

    @Test
    public void testConcurrent_safeRemoveAll() {
        final SubscribableSet<String> set = SubscribableSet.wrapSafe(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRemoveAll(Sets.newHashSet("a", "c")); }
        }, 20);
        assertEquals(set, Sets.newHashSet("b"));
    }

    @Test
    public void testConcurrent_safeRetainAll() {
        final SubscribableSet<String> set = SubscribableSet.wrapSafe(createSlowBackingSet());
        runInThreads(new Runnable() {
            @Override public void run() { set.standardRetainAll(Sets.newHashSet("a", "d")); }
        }, 20);
        assertEquals(set, Sets.newHashSet("a"));
    }

}
