package junit.framework

/**
 * A Listener for test progress
 */
interface TestListener {
    /**
     * An error occurred.
     */
    fun addError(test: Test, e: Throwable)

    /**
     * A failure occurred.
     */
    fun addFailure(test: Test, e: AssertionFailedError)

    /**
     * A test ended.
     */
    fun endTest(test: Test)

    /**
     * A test started.
     */
    fun startTest(test: Test)
}