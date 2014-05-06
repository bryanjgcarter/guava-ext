package com.toonetown.guava_ext.testing;

import org.slf4j.LoggerFactory;

import static org.testng.Assert.*;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.common.collect.Maps;

/**
 * An autocloseable class that disables logging for a short period of time.  Use this by doing:
 * try (final LogSilencer s = new LogSilencer(...)) { ... }
 */
public class LogSilencer implements AutoCloseable {
    /**
     * A function which returns the logger for a given class
     */
    private static Logger getLogger(final Class c) {
        return Logger.getLogger(LoggerFactory.getLogger(c).getName());
    }
    
    /** Our saved levels */
    private final Map<Class, Level> savedLevels = Maps.newHashMap();
    
    /** A lock we will use so only one thread can  */
    transient private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Our constructor
     *
     * @param silenceClasses the classes to silence
     */
    public LogSilencer(final Class... silenceClasses) {
        lock.lock();
        for (final Class c : silenceClasses) {
            final Logger l = getLogger(c);
            savedLevels.put(c, l.getLevel());
            l.setLevel(Level.OFF);
        }
    }
    
    /**
     * The close function which resets all logger levels
     */
    @Override public void close() {
        for (final Map.Entry<Class, Level> e : savedLevels.entrySet()) {
            getLogger(e.getKey()).setLevel(e.getValue());
        }
        lock.unlock();
    }
}
