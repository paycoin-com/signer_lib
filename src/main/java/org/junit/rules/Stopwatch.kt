package org.junit.rules

import org.junit.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runners.model.Statement

import java.util.concurrent.TimeUnit

/**
 * The Stopwatch Rule notifies one of its own protected methods of the time spent by a test.

 *
 * Override them to get the time in nanoseconds. For example, this class will keep logging the
 * time spent by each passed, failed, skipped, and finished test:

 *
 * public static class StopwatchTest {
 * private static final Logger logger = Logger.getLogger(&quot;&quot;);

 * private static void logInfo(Description description, String status, long nanos) {
 * String testName = description.getMethodName();
 * logger.info(String.format(&quot;Test %s %s, spent %d microseconds&quot;,
 * testName, status, TimeUnit.NANOSECONDS.toMicros(nanos)));
 * }

 * &#064;Rule
 * public Stopwatch stopwatch = new Stopwatch() {
 * &#064;Override
 * protected void succeeded(long nanos, Description description) {
 * logInfo(description, &quot;succeeded&quot;, nanos);
 * }

 * &#064;Override
 * protected void failed(long nanos, Throwable e, Description description) {
 * logInfo(description, &quot;failed&quot;, nanos);
 * }

 * &#064;Override
 * protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
 * logInfo(description, &quot;skipped&quot;, nanos);
 * }

 * &#064;Override
 * protected void finished(long nanos, Description description) {
 * logInfo(description, &quot;finished&quot;, nanos);
 * }
 * };

 * &#064;Test
 * public void succeeds() {
 * }

 * &#064;Test
 * public void fails() {
 * fail();
 * }

 * &#064;Test
 * public void skips() {
 * assumeTrue(false);
 * }
 * }
 *

 * An example to assert runtime:
 *
 * &#064;Test
 * public void performanceTest() throws InterruptedException {
 * long delta = 30;
 * Thread.sleep(300L);
 * assertEquals(300d, stopwatch.runtime(MILLISECONDS), delta);
 * Thread.sleep(500L);
 * assertEquals(800d, stopwatch.runtime(MILLISECONDS), delta);
 * }
 *

 * @author tibor17
 * *
 * @since 4.12
 */
abstract class Stopwatch internal constructor(private val clock: Stopwatch.Clock) : TestRule {
    @Volatile private var startNanos: Long = 0
    @Volatile private var endNanos: Long = 0

    constructor() : this(Clock()) {
    }

    /**
     * Gets the runtime for the test.

     * @param unit time unit for returned runtime
     * *
     * @return runtime measured during the test
     */
    fun runtime(unit: TimeUnit): Long {
        return unit.convert(nanos, TimeUnit.NANOSECONDS)
    }

    /**
     * Invoked when a test succeeds
     */
    protected fun succeeded(nanos: Long, description: Description) {
    }

    /**
     * Invoked when a test fails
     */
    protected fun failed(nanos: Long, e: Throwable, description: Description) {
    }

    /**
     * Invoked when a test is skipped due to a failed assumption.
     */
    protected fun skipped(nanos: Long, e: AssumptionViolatedException, description: Description) {
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    protected fun finished(nanos: Long, description: Description) {
    }

    private // volatile read happens here
    val nanos: Long
        get() {
            if (startNanos == 0) {
                throw IllegalStateException("Test has not started")
            }
            var currentEndNanos = endNanos
            if (currentEndNanos == 0) {
                currentEndNanos = clock.nanoTime()
            }

            return currentEndNanos - startNanos
        }

    private fun starting() {
        startNanos = clock.nanoTime()
        endNanos = 0
    }

    private fun stopping() {
        endNanos = clock.nanoTime()
    }

    override fun apply(base: Statement, description: Description): Statement {
        return InternalWatcher().apply(base, description)
    }

    private inner class InternalWatcher : TestWatcher() {

        override fun starting(description: Description) {
            this@Stopwatch.starting()
        }

        override fun finished(description: Description) {
            this@Stopwatch.finished(nanos, description)
        }

        override fun succeeded(description: Description) {
            this@Stopwatch.stopping()
            this@Stopwatch.succeeded(nanos, description)
        }

        override fun failed(e: Throwable, description: Description) {
            this@Stopwatch.stopping()
            this@Stopwatch.failed(nanos, e, description)
        }

        override fun skipped(e: AssumptionViolatedException, description: Description) {
            this@Stopwatch.stopping()
            this@Stopwatch.skipped(nanos, e, description)
        }
    }

    internal class Clock {

        fun nanoTime(): Long {
            return System.nanoTime()
        }
    }
}
