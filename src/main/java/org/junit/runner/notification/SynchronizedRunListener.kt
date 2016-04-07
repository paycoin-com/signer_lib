package org.junit.runner.notification

import org.junit.runner.Description
import org.junit.runner.Result

/**
 * Thread-safe decorator for [RunListener] implementations that synchronizes
 * calls to the delegate.

 *
 * This class synchronizes all listener calls on a RunNotifier instance. This is done because
 * prior to JUnit 4.12, all listeners were called in a synchronized block in RunNotifier,
 * so no two listeners were ever called concurrently. If we instead made the methods here
 * sychronized, clients that added multiple listeners that called common code might see
 * issues due to the reduced synchronization.

 * @author Tibor Digana (tibor17)
 * *
 * @author Kevin Cooney (kcooney)
 * *
 * @since 4.12
 * *
 * *
 * @see RunNotifier
 */
@RunListener.ThreadSafe
internal class SynchronizedRunListener(private val listener: RunListener, private val monitor: Any) : RunListener() {

    @Throws(Exception::class)
    override fun testRunStarted(description: Description) {
        synchronized (monitor) {
            listener.testRunStarted(description)
        }
    }

    @Throws(Exception::class)
    override fun testRunFinished(result: Result) {
        synchronized (monitor) {
            listener.testRunFinished(result)
        }
    }

    @Throws(Exception::class)
    override fun testStarted(description: Description) {
        synchronized (monitor) {
            listener.testStarted(description)
        }
    }

    @Throws(Exception::class)
    override fun testFinished(description: Description) {
        synchronized (monitor) {
            listener.testFinished(description)
        }
    }

    @Throws(Exception::class)
    override fun testFailure(failure: Failure) {
        synchronized (monitor) {
            listener.testFailure(failure)
        }
    }

    override fun testAssumptionFailure(failure: Failure) {
        synchronized (monitor) {
            listener.testAssumptionFailure(failure)
        }
    }

    @Throws(Exception::class)
    override fun testIgnored(description: Description) {
        synchronized (monitor) {
            listener.testIgnored(description)
        }
    }

    override fun hashCode(): Int {
        return listener.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SynchronizedRunListener) {
            return false
        }

        return listener == other.listener
    }

    override fun toString(): String {
        return listener.toString() + " (with synchronization wrapper)"
    }
}
