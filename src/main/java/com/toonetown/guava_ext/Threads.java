package com.toonetown.guava_ext;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.base.Function;

/**
 * A static class for handling thread and concurrency tasks
 */
public class Threads {
    private Threads() {}
    
    /**
     * Returns a ListeningExecutorService based off of the global ThreadFactory
     */
    public static ListeningExecutorService executor() {
        final ExecutorService svc = Executors.newCachedThreadPool(MoreExecutors.platformThreadFactory());
        return MoreExecutors.listeningDecorator(svc);
    }
    
    /**
     * Returns a ListenableFuture for the given Async object
     */
    public static <T> ListenableFuture<T> future(final Async<T> async) {
        return executor().submit(async);
    }
    
    /**
     * Returns a CheckedFuture for the given CheckedAsync object
     */
    public static <T, X extends Exception> CheckedFuture<T, X> checkedFuture(final CheckedAsync<T, X> async) {
        return Futures.makeChecked(future(async), async);
    }
    
    /**
     * A class which can be extended in order to return ListenableFutures. 
     */
    public static abstract class Async<T> implements Callable<T> { }
    
    /**
     * A class which can be extended in order to return CheckedFutures
     */
    @RequiredArgsConstructor
    public static abstract class CheckedAsync<T, X> extends Async<T> implements Function<Exception, X> {
        private final Class<X> exceptionClass;
        @Override public X apply(final Exception e) {
            Throwable t = e;
            while (t != null && t instanceof ExecutionException) {
                t = t.getCause();
            }
            return exceptionClass.cast(t);
        }
    }
    
}
