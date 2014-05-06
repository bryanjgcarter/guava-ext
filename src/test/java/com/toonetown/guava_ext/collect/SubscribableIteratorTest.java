package com.toonetown.guava_ext.collect;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import com.toonetown.guava_ext.testing.DataProviders;
import com.toonetown.guava_ext.eventbus.Events;
import com.toonetown.guava_ext.eventbus.Subscribable;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit test for SubscribableIterator
 */
public class SubscribableIteratorTest {
    /**
     * A static class which counts events
     */
    public static class CountingListener<T> {
        public CountingListener(final Subscribable s) {
            s.register(this);
        }
        
        private Set<T> changes = Sets.newHashSet();
        private Set<T> removes = Sets.newHashSet();

        @Subscribe public void onChangeEvent(final Events.ChangeEvent<SubscribableIterator<T>, T> event) {
            changes.add(event.element());
        }
        @Subscribe public void onRemoveEvent(final Events.RemoveEvent<SubscribableIterator<T>, T> event) {
            removes.add(event.element());
        }
        
        public void assertExpected(final Set<T> expectedRemoves) {
            assertEquals(this.changes, expectedRemoves);
            assertEquals(this.removes, expectedRemoves);
        }
    }

    public static DataProviders.TestSet getIterableData() {
        final Set<String> s = Sets.newHashSet("a", "b", "c");
        final SubscribableIterator<String> iter = SubscribableIterator.wrap(s.iterator());
        return tests(params(iter, new CountingListener<String>(iter), s));
    }

    @DataProvider(name = "iterableData", parallel = true)
    public Object[][] iterableData() { return getIterableData().create(); }

    @Test(dataProvider = "iterableData")
    public void testRemove(final Iterator<String> iter,
                           final CountingListener<String> listener,
                           final Set<String> set) {
        iter.next();
        final String second = iter.next();
        iter.remove();
        listener.assertExpected(Sets.newHashSet(second));
        assertEquals(set.size(), 2);
    }

    @Test(dataProvider = "iterableData")
    public void testRemoveAll(final Iterator<String> iter,
                              final CountingListener<String> listener,
                              final Set<String> set) {
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        listener.assertExpected(Sets.newHashSet("a", "b", "c"));
        assertEquals(set.size(), 0);
    }    
}
