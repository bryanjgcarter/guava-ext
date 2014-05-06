package com.toonetown.guava_ext.testing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.toonetown.guava_ext.collect.LookBackIterator;

/**
 * A static class which allows merging of collections either using a cartesian product or a concatenation
 */
public class Merger {
    public interface Mergeable<T> extends Collection<T> { }
    public interface Creator<T extends Mergeable<M>, M> extends Function<Iterable<M>, T> { }

    /**
     * Returns the cartesian product of the given lists.  The resulting iterable will contain every possible
     * combination of one value from each parameter (or test) list.
     *
     * @param lists the lists to get the product of
     * @return the cartesian product of all lists
     */
    public static <T extends Mergeable<M>, M> Iterable<T> product(final Iterable<T> lists,
                                                                  final Creator<T, M> creator) {
        return (new Function<Iterable<T>, Iterable<T>>() {
            @Override public Iterable<T> apply(final Iterable<T> input) {
                if (input == null || Iterables.isEmpty(input)) { return Collections.emptySet(); }

                /* Create a map of all our iterators */
                final Map<T, LookBackIterator<M>> map = Maps.newLinkedHashMap();
                for (final T list : input) {
                    if (!list.isEmpty()) {  map.put(list, LookBackIterator.wrap(list.iterator())); }
                }
                if (map.isEmpty()) { return Collections.emptySet(); }

                return ImmutableList.copyOf(new AbstractIterator<T>() {
                    @Override protected T computeNext() {
                        /* Roll the next one as needed */
                        boolean hasNext = false;
                        for (final Map.Entry<T, LookBackIterator<M>> entry : map.entrySet()) {
                            if (entry.getValue().hasNext()) {
                                entry.getValue().next();
                                hasNext = true;
                                break;
                            }
                            /* It didn't have a next - so reset this one, and we will try and roll the next one */
                            entry.setValue(LookBackIterator.wrap(entry.getKey().iterator()));
                        }
                        if (!hasNext) {
                            return endOfData();
                        }
                        return creator.apply(Iterables.filter(Iterables.transform(map.values(),
                            new Function<LookBackIterator<M>, M>() {
                                @Override public M apply(final LookBackIterator<M> paramIter) {
                                    return paramIter.lastOrNext();
                                }
                            }), Predicates.notNull()));
                    }
                });
            }
        }).apply(lists);
    }

    public static <T extends Mergeable<M>, M> T concat(final Iterable<T> lists, final Creator<T, M> creator) {
        return (new Function<Iterable<T>, T>() {
            @Override public T apply(final Iterable<T> input) {
                return creator.apply(Iterables.concat(Iterables.filter(input, Predicates.notNull())));
            }
        }).apply(lists);
    }

    @SafeVarargs
    public static <T> Iterable<T> toIterable(final T... lists) {
        final List<T> returnLists = Lists.newArrayList();
        for (final T list : lists) { returnLists.add(list); }
        return returnLists;
    }
}
