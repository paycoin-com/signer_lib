package junit.framework

import java.util.ArrayList
import java.util.Collections
import java.util.Enumeration

/**
 * A `TestResult` collects the results of executing
 * a test case. It is an instance of the Collecting Parameter pattern.
 * The test framework distinguishes between *failures* and *errors*.
 * A failure is anticipated and checked for with assertions. Errors are
 * unanticipated problems like an [ArrayIndexOutOfBoundsException].

 * @see Test
 */
class TestResult {
    protected var fFailures: MutableList<TestFailure>
    protected var fErrors: MutableList<TestFailure>
    protected var fListeners: MutableList<TestListener>
    protected var fRunTests: Int = 0
    private var fStop: Boolean = false

    init {
        fFailures = ArrayList<TestFailure>()
        fErrors = ArrayList<TestFailure>()
        fListeners = ArrayList<TestListener>()
        fRunTests = 0
        fStop = false
    }

    /**
     * Adds an error to the list of errors. The passed in exception
     * caused the error.
     */
    @Synchronized fun addError(test: Test, e: Throwable) {
        fErrors.add(TestFailure(test, e))
        for (each in cloneListeners()) {
            each.addError(test, e)
        }
    }

    /**
     * Adds a failure to the list of failures. The passed in exception
     * caused the failure.
     */
    @Synchronized fun addFailure(test: Test, e: AssertionFailedError) {
        fFailures.add(TestFailure(test, e))
        for (each in cloneListeners()) {
            each.addFailure(test, e)
        }
    }

    /**
     * Registers a TestListener
     */
    @Synchronized fun addListener(listener: TestListener) {
        fListeners.add(listener)
    }

    /**
     * Unregisters a TestListener
     */
    @Synchronized fun removeListener(listener: TestListener) {
        fListeners.remove(listener)
    }

    /**
     * Returns a copy of the listeners.
     */
    @Synchronized private fun cloneListeners(): List<TestListener> {
        val result = ArrayList<TestListener>()
        result.addAll(fListeners)
        return result
    }

    /**
     * Informs the result that a test was completed.
     */
    fun endTest(test: Test) {
        for (each in cloneListeners()) {
            each.endTest(test)
        }
    }

    /**
     * Gets the number of detected errors.
     */
    @Synchronized fun errorCount(): Int {
        return fErrors.size
    }

    /**
     * Returns an Enumeration for the errors
     */
    @Synchronized fun errors(): Enumeration<TestFailure> {
        return Collections.enumeration(fErrors)
    }


    /**
     * Gets the number of detected failures.
     */
    @Synchronized fun failureCount(): Int {
        return fFailures.size
    }

    /**
     * Returns an Enumeration for the failures
     */
    @Synchronized fun failures(): Enumeration<TestFailure> {
        return Collections.enumeration(fFailures)
    }

    /**
     * Runs a TestCase.
     */
    protected fun run(test: TestCase) {
        startTest(test)
        val p = Protectable { test.runBare() }
        runProtected(test, p)

        endTest(test)
    }

    /**
     * Gets the number of run tests.
     */
    @Synchronized fun runCount(): Int {
        return fRunTests
    }

    /**
     * Runs a TestCase.
     */
    fun runProtected(test: Test, p: Protectable) {
        try {
            p.protect()
        } catch (e: AssertionFailedError) {
            addFailure(test, e)
        } catch (e: ThreadDeath) {
            // don't catch ThreadDeath by accident
            throw e
        } catch (e: Throwable) {
            addError(test, e)
        }

    }

    /**
     * Checks whether the test run should stop
     */
    @Synchronized fun shouldStop(): Boolean {
        return fStop
    }

    /**
     * Informs the result that a test will be started.
     */
    fun startTest(test: Test) {
        val count = test.countTestCases()
        synchronized (this) {
            fRunTests += count
        }
        for (each in cloneListeners()) {
            each.startTest(test)
        }
    }

    /**
     * Marks that the test run should stop.
     */
    @Synchronized fun stop() {
        fStop = true
    }

    /**
     * Returns whether the entire test was successful or not.
     */
    @Synchronized fun wasSuccessful(): Boolean {
        return failureCount() == 0 && errorCount() == 0
    }
}
