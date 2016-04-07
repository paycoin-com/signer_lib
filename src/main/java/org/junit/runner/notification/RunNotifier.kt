package org.junit.runner.notification

import java.util.Arrays.asList

import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

import org.junit.runner.Description
import org.junit.runner.Result

/**
 * If you write custom runners, you may need to notify JUnit of your progress running tests.
 * Do this by invoking the `RunNotifier` passed to your implementation of
 * [org.junit.runner.Runner.run]. Future evolution of this class is likely to
 * move [.fireTestRunStarted] and [.fireTestRunFinished]
 * to a separate class since they should only be called once per run.

 * @since 4.0
 */
class RunNotifier {
    private val listeners = CopyOnWriteArrayList<RunListener>()
    @Volatile private var pleaseStop = false

    /**
     * Internal use only
     */
    fun addListener(listener: RunListener?) {
        if (listener == null) {
            throw NullPointerException("Cannot add a null listener")
        }
        listeners.add(wrapIfNotThreadSafe(listener))
    }

    /**
     * Internal use only
     */
    fun removeListener(listener: RunListener?) {
        if (listener == null) {
            throw NullPointerException("Cannot remove a null listener")
        }
        listeners.remove(wrapIfNotThreadSafe(listener))
    }

    /**
     * Wraps the given listener with [SynchronizedRunListener] if
     * it is not annotated with [RunListener.ThreadSafe].
     */
    internal fun wrapIfNotThreadSafe(listener: RunListener): RunListener {
        return if (listener.javaClass.isAnnotationPresent(RunListener.ThreadSafe::class.java))
            listener
        else
            SynchronizedRunListener(listener, this)
    }


    private abstract inner class SafeNotifier @JvmOverloads internal constructor(private val currentListeners: List<RunListener> = listeners) {

        internal fun run() {
            val capacity = currentListeners.size
            val safeListeners = ArrayList<RunListener>(capacity)
            val failures = ArrayList<Failure>(capacity)
            for (listener in currentListeners) {
                try {
                    notifyListener(listener)
                    safeListeners.add(listener)
                } catch (e: Exception) {
                    failures.add(Failure(Description.TEST_MECHANISM, e))
                }

            }
            fireTestFailures(safeListeners, failures)
        }

        @Throws(Exception::class)
        protected abstract fun notifyListener(each: RunListener)
    }

    /**
     * Do not invoke.
     */
    fun fireTestRunStarted(description: Description) {
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testRunStarted(description)
            }
        }.run()
    }

    /**
     * Do not invoke.
     */
    fun fireTestRunFinished(result: Result) {
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testRunFinished(result)
            }
        }.run()
    }

    /**
     * Invoke to tell listeners that an atomic test is about to start.

     * @param description the description of the atomic test (generally a class and method name)
     * *
     * @throws StoppedByUserException thrown if a user has requested that the test run stop
     */
    @Throws(StoppedByUserException::class)
    fun fireTestStarted(description: Description) {
        if (pleaseStop) {
            throw StoppedByUserException()
        }
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testStarted(description)
            }
        }.run()
    }

    /**
     * Invoke to tell listeners that an atomic test failed.

     * @param failure the description of the test that failed and the exception thrown
     */
    fun fireTestFailure(failure: Failure) {
        fireTestFailures(listeners, asList(failure))
    }

    private fun fireTestFailures(listeners: List<RunListener>,
                                 failures: List<Failure>) {
        if (!failures.isEmpty()) {
            object : SafeNotifier(listeners) {
                @Throws(Exception::class)
                override fun notifyListener(listener: RunListener) {
                    for (each in failures) {
                        listener.testFailure(each)
                    }
                }
            }.run()
        }
    }

    /**
     * Invoke to tell listeners that an atomic test flagged that it assumed
     * something false.

     * @param failure the description of the test that failed and the
     * * [org.junit.AssumptionViolatedException] thrown
     */
    fun fireTestAssumptionFailed(failure: Failure) {
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testAssumptionFailure(failure)
            }
        }.run()
    }

    /**
     * Invoke to tell listeners that an atomic test was ignored.

     * @param description the description of the ignored test
     */
    fun fireTestIgnored(description: Description) {
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testIgnored(description)
            }
        }.run()
    }

    /**
     * Invoke to tell listeners that an atomic test finished. Always invoke
     * this method if you invoke [.fireTestStarted]
     * as listeners are likely to expect them to come in pairs.

     * @param description the description of the test that finished
     */
    fun fireTestFinished(description: Description) {
        object : SafeNotifier() {
            @Throws(Exception::class)
            override fun notifyListener(each: RunListener) {
                each.testFinished(description)
            }
        }.run()
    }

    /**
     * Ask that the tests run stop before starting the next test. Phrased politely because
     * the test currently running will not be interrupted. It seems a little odd to put this
     * functionality here, but the `RunNotifier` is the only object guaranteed
     * to be shared amongst the many runners involved.
     */
    fun pleaseStop() {
        pleaseStop = true
    }

    /**
     * Internal use only. The Result's listener must be first.
     */
    fun addFirstListener(listener: RunListener?) {
        if (listener == null) {
            throw NullPointerException("Cannot add a null listener")
        }
        listeners.add(0, wrapIfNotThreadSafe(listener))
    }
}
