package org.junit.internal.runners.statements

import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.util.Arrays
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.junit.runners.model.MultipleFailureException
import org.junit.runners.model.Statement
import org.junit.runners.model.TestTimedOutException

class FailOnTimeout private constructor(builder: FailOnTimeout.Builder, private val originalStatement: Statement) : Statement() {
    private val timeUnit: TimeUnit
    private val timeout: Long
    private val lookForStuckThread: Boolean
    @Volatile private var threadGroup: ThreadGroup? = null

    /**
     * Creates an instance wrapping the given statement with the given timeout in milliseconds.

     * @param statement the statement to wrap
     * *
     * @param timeoutMillis the timeout in milliseconds
     * *
     */
    @Deprecated("")
    @Deprecated("use {@link #builder()} instead.")
    constructor(statement: Statement, timeoutMillis: Long) : this(builder().withTimeout(timeoutMillis, TimeUnit.MILLISECONDS), statement) {
    }

    init {
        timeout = builder.timeout
        timeUnit = builder.unit
        lookForStuckThread = builder.lookForStuckThread
    }

    /**
     * Builder for [FailOnTimeout].

     * @since 4.12
     */
    class Builder private constructor() {
        private var lookForStuckThread = false
        private var timeout: Long = 0
        private var unit = TimeUnit.SECONDS

        /**
         * Specifies the time to wait before timing out the test.

         *
         * If this is not called, or is called with a `timeout` of
         * `0`, the returned `Statement` will wait forever for the
         * test to complete, however the test will still launch from a separate
         * thread. This can be useful for disabling timeouts in environments
         * where they are dynamically set based on some property.

         * @param timeout the maximum time to wait
         * *
         * @param unit the time unit of the `timeout` argument
         * *
         * @return `this` for method chaining.
         */
        fun withTimeout(timeout: Long, unit: TimeUnit?): Builder {
            if (timeout < 0) {
                throw IllegalArgumentException("timeout must be non-negative")
            }
            if (unit == null) {
                throw NullPointerException("TimeUnit cannot be null")
            }
            this.timeout = timeout
            this.unit = unit
            return this
        }

        /**
         * Specifies whether to look for a stuck thread.  If a timeout occurs and this
         * feature is enabled, the test will look for a thread that appears to be stuck
         * and dump its backtrace.  This feature is experimental.  Behavior may change
         * after the 4.12 release in response to feedback.

         * @param enable `true` to enable the feature
         * *
         * @return `this` for method chaining.
         */
        fun withLookingForStuckThread(enable: Boolean): Builder {
            this.lookForStuckThread = enable
            return this
        }

        /**
         * Builds a [FailOnTimeout] instance using the values in this builder,
         * wrapping the given statement.

         * @param statement
         */
        fun build(statement: Statement?): FailOnTimeout {
            if (statement == null) {
                throw NullPointerException("statement cannot be null")
            }
            return FailOnTimeout(this, statement)
        }
    }

    @Throws(Throwable::class)
    override fun evaluate() {
        val callable = CallableStatement()
        val task = FutureTask(callable)
        threadGroup = ThreadGroup("FailOnTimeoutGroup")
        val thread = Thread(threadGroup, task, "Time-limited test")
        thread.isDaemon = true
        thread.start()
        callable.awaitStarted()
        val throwable = getResult(task, thread)
        if (throwable != null) {
            throw throwable
        }
    }

    /**
     * Wait for the test task, returning the exception thrown by the test if the
     * test failed, an exception indicating a timeout if the test timed out, or
     * `null` if the test passed.
     */
    private fun getResult(task: FutureTask<Throwable>, thread: Thread): Throwable? {
        try {
            if (timeout > 0) {
                return task.get(timeout, timeUnit)
            } else {
                return task.get()
            }
        } catch (e: InterruptedException) {
            return e // caller will re-throw; no need to call Thread.interrupt()
        } catch (e: ExecutionException) {
            // test failed; have caller re-throw the exception thrown by the test
            return e.cause
        } catch (e: TimeoutException) {
            return createTimeoutException(thread)
        }

    }

    private fun createTimeoutException(thread: Thread): Exception {
        val stackTrace = thread.stackTrace
        val stuckThread = if (lookForStuckThread) getStuckThread(thread) else null
        val currThreadException = TestTimedOutException(timeout, timeUnit)
        if (stackTrace != null) {
            currThreadException.setStackTrace(stackTrace)
            thread.interrupt()
        }
        if (stuckThread != null) {
            val stuckThreadException = Exception("Appears to be stuck in thread " + stuckThread.name)
            stuckThreadException.setStackTrace(getStackTrace(stuckThread))
            return MultipleFailureException(
                    Arrays.asList<Throwable>(currThreadException, stuckThreadException))
        } else {
            return currThreadException
        }
    }

    /**
     * Retrieves the stack trace for a given thread.
     * @param thread The thread whose stack is to be retrieved.
     * *
     * @return The stack trace; returns a zero-length array if the thread has
     * * terminated or the stack cannot be retrieved for some other reason.
     */
    private fun getStackTrace(thread: Thread): Array<StackTraceElement> {
        try {
            return thread.stackTrace
        } catch (e: SecurityException) {
            return arrayOfNulls(0)
        }

    }

    /**
     * Determines whether the test appears to be stuck in some thread other than
     * the "main thread" (the one created to run the test).  This feature is experimental.
     * Behavior may change after the 4.12 release in response to feedback.
     * @param mainThread The main thread created by `evaluate()`
     * *
     * @return The thread which appears to be causing the problem, if different from
     * * `mainThread`, or `null` if the main thread appears to be the
     * * problem or if the thread cannot be determined.  The return value is never equal
     * * to `mainThread`.
     */
    private fun getStuckThread(mainThread: Thread): Thread? {
        if (threadGroup == null) {
            return null
        }
        val threadsInGroup = getThreadArray(threadGroup) ?: return null

        // Now that we have all the threads in the test's thread group: Assume that
        // any thread we're "stuck" in is RUNNABLE.  Look for all RUNNABLE threads. 
        // If just one, we return that (unless it equals threadMain).  If there's more
        // than one, pick the one that's using the most CPU time, if this feature is
        // supported.
        var stuckThread: Thread? = null
        var maxCpuTime: Long = 0
        for (thread in threadsInGroup) {
            if (thread.state == Thread.State.RUNNABLE) {
                val threadCpuTime = cpuTime(thread)
                if (stuckThread == null || threadCpuTime > maxCpuTime) {
                    stuckThread = thread
                    maxCpuTime = threadCpuTime
                }
            }
        }
        return if (stuckThread === mainThread) null else stuckThread
    }

    /**
     * Returns all active threads belonging to a thread group.
     * @param group The thread group.
     * *
     * @return The active threads in the thread group.  The result should be a
     * * complete list of the active threads at some point in time.  Returns `null`
     * * if this cannot be determined, e.g. because new threads are being created at an
     * * extremely fast rate.
     */
    private fun getThreadArray(group: ThreadGroup): Array<Thread>? {
        val count = group.activeCount() // this is just an estimate
        var enumSize = Math.max(count * 2, 100)
        var enumCount: Int
        var threads: Array<Thread>
        var loopCount = 0
        while (true) {
            threads = arrayOfNulls<Thread>(enumSize)
            enumCount = group.enumerate(threads)
            if (enumCount < enumSize) {
                break
            }
            // if there are too many threads to fit into the array, enumerate's result
            // is >= the array's length; therefore we can't trust that it returned all
            // the threads.  Try again.
            enumSize += 100
            if (++loopCount >= 5) {
                return null
            }
            // threads are proliferating too fast for us.  Bail before we get into 
            // trouble.
        }
        return copyThreads(threads, enumCount)
    }

    /**
     * Returns an array of the first `count` Threads in `threads`.
     * (Use instead of Arrays.copyOf to maintain compatibility with Java 1.5.)
     * @param threads The source array.
     * *
     * @param count The maximum length of the result array.
     * *
     * @return The first {@count} (at most) elements of `threads`.
     */
    private fun copyThreads(threads: Array<Thread>, count: Int): Array<Thread> {
        val length = Math.min(count, threads.size)
        val result = arrayOfNulls<Thread>(length)
        for (i in 0..length - 1) {
            result[i] = threads[i]
        }
        return result
    }

    /**
     * Returns the CPU time used by a thread, if possible.
     * @param thr The thread to query.
     * *
     * @return The CPU time used by `thr`, or 0 if it cannot be determined.
     */
    private fun cpuTime(thr: Thread): Long {
        val mxBean = ManagementFactory.getThreadMXBean()
        if (mxBean.isThreadCpuTimeSupported) {
            try {
                return mxBean.getThreadCpuTime(thr.id)
            } catch (e: UnsupportedOperationException) {
            }

        }
        return 0
    }

    private inner class CallableStatement : Callable<Throwable> {
        private val startLatch = CountDownLatch(1)

        @Throws(Exception::class)
        override fun call(): Throwable? {
            try {
                startLatch.countDown()
                originalStatement.evaluate()
            } catch (e: Exception) {
                throw e
            } catch (e: Throwable) {
                return e
            }

            return null
        }

        @Throws(InterruptedException::class)
        fun awaitStarted() {
            startLatch.await()
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
    }
}
