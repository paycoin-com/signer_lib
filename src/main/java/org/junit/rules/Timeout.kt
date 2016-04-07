package org.junit.rules

import org.junit.internal.runners.statements.FailOnTimeout
import org.junit.runner.Description
import org.junit.runners.model.Statement

import java.util.concurrent.TimeUnit

/**
 * The Timeout Rule applies the same timeout to all test methods in a class:
 *
 * public static class HasGlobalLongTimeout {

 * &#064;Rule
 * public Timeout globalTimeout= new Timeout(20);

 * &#064;Test
 * public void run1() throws InterruptedException {
 * Thread.sleep(100);
 * }

 * &#064;Test
 * public void infiniteLoop() {
 * while (true) {}
 * }
 * }
 *
 *
 *
 * Each test is run in a new thread. If the specified timeout elapses before
 * the test completes, its execution is interrupted via [Thread.interrupt].
 * This happens in interruptable I/O and locks, and methods in [Object]
 * and [Thread] throwing [InterruptedException].
 *
 *
 * A specified timeout of 0 will be interpreted as not set, however tests will
 * still launch from separate threads. This can be useful for disabling timeouts
 * in environments where they are dynamically set based on some property.

 * @since 4.7
 */
class Timeout : TestRule {
    private val timeout: Long
    private val timeUnit: TimeUnit
    /**
     * Gets whether this `Timeout` will look for a stuck thread
     * when the test times out.

     * @since 4.12
     */
    protected val lookingForStuckThread: Boolean

    /**
     * Create a `Timeout` instance with the timeout specified
     * in milliseconds.
     *
     *
     * This constructor is deprecated.
     *
     *
     * Instead use [.Timeout],
     * [Timeout.millis], or [Timeout.seconds].

     * @param millis the maximum time in milliseconds to allow the
     * * test to run before it should timeout
     */
    @Deprecated("")
    constructor(millis: Int) : this(millis.toLong(), TimeUnit.MILLISECONDS) {
    }

    /**
     * Create a `Timeout` instance with the timeout specified
     * at the timeUnit of granularity of the provided `TimeUnit`.

     * @param timeout the maximum time to allow the test to run
     * * before it should timeout
     * *
     * @param timeUnit the time unit for the `timeout`
     * *
     * @since 4.12
     */
    constructor(timeout: Long, timeUnit: TimeUnit) {
        this.timeout = timeout
        this.timeUnit = timeUnit
        lookingForStuckThread = false
    }

    /**
     * Create a `Timeout` instance initialized with values form
     * a builder.

     * @since 4.12
     */
    protected constructor(builder: Builder) {
        timeout = builder.timeout
        timeUnit = builder.timeUnit
        lookingForStuckThread = builder.lookingForStuckThread
    }

    /**
     * Gets the timeout configured for this rule, in the given units.

     * @since 4.12
     */
    protected fun getTimeout(unit: TimeUnit): Long {
        return unit.convert(timeout, timeUnit)
    }

    /**
     * Creates a [Statement] that will run the given
     * `statement`, and timeout the operation based
     * on the values configured in this rule. Subclasses
     * can override this method for different behavior.

     * @since 4.12
     */
    @Throws(Exception::class)
    protected fun createFailOnTimeoutStatement(
            statement: Statement): Statement {
        return FailOnTimeout.builder().withTimeout(timeout, timeUnit).withLookingForStuckThread(lookingForStuckThread).build(statement)
    }

    override fun apply(base: Statement, description: Description): Statement {
        try {
            return createFailOnTimeoutStatement(base)
        } catch (e: Exception) {
            return object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    throw RuntimeException("Invalid parameters for Timeout", e)
                }
            }
        }

    }

    /**
     * Builder for [Timeout].

     * @since 4.12
     */
    class Builder protected constructor() {
        protected var lookingForStuckThread = false
            private set
        protected var timeout: Long = 0
            private set
        protected var timeUnit = TimeUnit.SECONDS
            private set

        /**
         * Specifies the time to wait before timing out the test.

         *
         * If this is not called, or is called with a
         * `timeout` of `0`, the returned `Timeout`
         * rule instance will cause the tests to wait forever to
         * complete, however the tests will still launch from a
         * separate thread. This can be useful for disabling timeouts
         * in environments where they are dynamically set based on
         * some property.

         * @param timeout the maximum time to wait
         * *
         * @param unit the time unit of the `timeout` argument
         * *
         * @return `this` for method chaining.
         */
        fun withTimeout(timeout: Long, unit: TimeUnit): Builder {
            this.timeout = timeout
            this.timeUnit = unit
            return this
        }

        /**
         * Specifies whether to look for a stuck thread.  If a timeout occurs and this
         * feature is enabled, the rule will look for a thread that appears to be stuck
         * and dump its backtrace.  This feature is experimental.  Behavior may change
         * after the 4.12 release in response to feedback.

         * @param enable `true` to enable the feature
         * *
         * @return `this` for method chaining.
         */
        fun withLookingForStuckThread(enable: Boolean): Builder {
            this.lookingForStuckThread = enable
            return this
        }


        /**
         * Builds a [Timeout] instance using the values in this builder.,
         */
        fun build(): Timeout {
            return Timeout(this)
        }
    }

    companion object {

        /**
         * Returns a new builder for building an instance.

         * @since 4.12
         */
        fun builder(): Builder {
            return Builder()
        }

        /**
         * Creates a [Timeout] that will timeout a test after the
         * given duration, in milliseconds.

         * @since 4.12
         */
        fun millis(millis: Long): Timeout {
            return Timeout(millis, TimeUnit.MILLISECONDS)
        }

        /**
         * Creates a [Timeout] that will timeout a test after the
         * given duration, in seconds.

         * @since 4.12
         */
        fun seconds(seconds: Long): Timeout {
            return Timeout(seconds, TimeUnit.SECONDS)
        }
    }
}
