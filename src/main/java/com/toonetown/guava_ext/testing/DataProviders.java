package com.toonetown.guava_ext.testing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Helper class for creating data providers.
 */
public class DataProviders {
    private DataProviders() { }

    /** Returns a list of classes */
    public static Iterable<Class<?>> types(final Class<?>... types) { return Iterables.cycle(types); }
    public static Iterable<Class<?>> types() { return types(Object.class); }

    /** Creates a parameter of the given type - casting the object (and throwing an exception) as needed */
    private static <T> Parameter param(final Class<T> type, final Object obj) throws ClassCastException {
        return new Parameter<T>(type, type.cast(obj));
    }
    /** Returns a function which checks that parameters match */
    private static Function<Object, Parameter<?>> paramChecker(final Iterable<Class<?>> types) {
        final Iterator<Class<?>> typeIter = types.iterator();
        return new Function<Object, Parameter<?>>() {
            @Override public Parameter<?> apply(final Object input) { return param(typeIter.next(), input); }
        };
    }

    /**
     * Returns a new parameter set with the given objects.
     */
    public static ParameterList typedParams(final Iterable<Class<?>> types, final Object... objs) {
        return new ParameterList(Iterables.transform(Lists.newArrayList(objs), paramChecker(types)));
    }
    public static ParameterList params(final Object... objs) { return typedParams(types(), objs); }

    public static Iterable<ParameterList> product(final ParameterList... lists) {
        return Merger.product(Merger.toIterable(lists), ParameterList.CREATOR);
    }
    public static ParameterList concat(final ParameterList... lists) {
        return Merger.concat(Merger.toIterable(lists), ParameterList.CREATOR);
    }

    /** Returns a new test set with the given parameter sets */
    public static TestSet tests(final Iterable<ParameterList> params) {
        return new TestSet(Iterables.filter(params, Predicates.notNull()));
    }
    public static TestSet tests(final ParameterList... params) { return tests(Sets.newHashSet(params)); }
    public static TestSet tests(final Object[][] objArrays) {
        final Set<ParameterList> paramSet = Sets.newHashSet();
        for (final Object[] objArray : objArrays) {
            paramSet.add(typedParams(types(), objArray));
        }
        return tests(paramSet);
    }

    public static TestSet product(final TestSet... sets) {
        return concat(Iterables.toArray(Merger.product(Merger.toIterable(sets),
                                                       new Merger.Creator<TestSet, ParameterList>() {
            @Override public TestSet apply(final Iterable<ParameterList> input) {
                return tests(concat(Iterables.toArray(input, ParameterList.class)));
            }
        }), TestSet.class));
    }
    public static TestSet concat(final TestSet... sets) {
        return Merger.concat(Merger.toIterable(sets), TestSet.CREATOR);
    }

    /** A class which represents a parameter */
    @Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Parameter<T> {
        private final Class<T> type;
        private final T obj;
    }

    /** A class which represents an ordered list of parameters */
    @Getter(AccessLevel.PROTECTED) @Accessors(fluent = true)
    public static class ParameterList extends ForwardingList<Parameter<?>>
    implements Function<Parameter, Object>, Merger.Mergeable<Parameter<?>> {
        private final List<Parameter<?>> delegate;
        private ParameterList(final Iterable<Parameter<?>> delegate) {
            if (delegate instanceof List) {
                this.delegate = (List<Parameter<?>>) delegate;
            } else {
                this.delegate = Lists.newArrayList(delegate);
            }
        }

        /** Creates an array of the objects */
        public Object[] create() { return Iterables.toArray(Iterables.transform(this, this), Object.class); }

        /** Returns the given parameter's object */
        @Override public Object apply(final Parameter input) { return input.getObj(); }

        /** Returns the given index, cast as the given class */
        public <T> T get(final int idx, final Class<T> clazz) { return clazz.cast(apply(get(idx))); }

        private static final Merger.Creator<ParameterList, Parameter<?>> CREATOR =
                new Merger.Creator<ParameterList, Parameter<?>>() {
                    @Override public ParameterList apply(final Iterable<Parameter<?>> input) {
                        return new ParameterList(input);
                    }
                };
    }

    /** A class which represents an unordered set of ParameterLists */
    @Getter(AccessLevel.PROTECTED) @Accessors(fluent = true)
    public static class TestSet extends ForwardingSet<ParameterList> implements Merger.Mergeable<ParameterList> {
        private final Set<ParameterList> delegate;

        private TestSet(final Iterable<ParameterList> delegate) {
            if (delegate instanceof Set) {
                this.delegate = (Set<ParameterList>) delegate;
            } else {
                this.delegate = Sets.newHashSet(delegate);
            }
        }


        /** Creates the DataProvider information */
        public Object[][] create() {
            if (isEmpty()) { return new Object[0][0]; }
            return Iterables.toArray(Iterables.transform(this, new Function<ParameterList, Object[]>() {
                @Override public Object[] apply(final ParameterList input) { return input.create(); }
            }), Object[].class);
        }

        private static final Merger.Creator<TestSet, ParameterList> CREATOR =
                new Merger.Creator<TestSet, ParameterList>() {
                    @Override public TestSet apply(final Iterable<ParameterList> input) {
                        return new TestSet(input);
                    }
                };
    }
}
