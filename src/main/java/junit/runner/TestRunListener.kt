package junit.runner

/**
 * A listener interface for observing the
 * execution of a test run. Unlike TestListener,
 * this interface using only primitive objects,
 * making it suitable for remote test execution.
 */
interface TestRunListener {

    fun testRunStarted(testSuiteName: String, testCount: Int)

    fun testRunEnded(elapsedTime: Long)

    fun testRunStopped(elapsedTime: Long)

    fun testStarted(testName: String)

    fun testEnded(testName: String)

    fun testFailed(status: Int, testName: String, trace: String)

    companion object {
        /* test status constants*/
        val STATUS_ERROR = 1
        val STATUS_FAILURE = 2
    }
}
