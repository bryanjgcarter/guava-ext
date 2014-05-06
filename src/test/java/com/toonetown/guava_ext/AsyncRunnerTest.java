package com.toonetown.guava_ext;

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.toonetown.guava_ext.AsyncRunner;
import com.toonetown.guava_ext.NotFoundException;

/**
 * Test for AsyncRunner
 */
@Slf4j
public class AsyncRunnerTest {
    @Test
    public void testRunner() throws NotFoundException {
        final AsyncRunner runner = new AsyncRunner(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error during test ", e);
                }
            }
        });
        assertFalse(runner.isRunning());
        assertTrue(runner.runAsync());
        assertFalse(runner.runAsync());
        assertTrue(runner.isRunning());
        runner.waitForCompletion();
        assertFalse(runner.isRunning());
    }
}
