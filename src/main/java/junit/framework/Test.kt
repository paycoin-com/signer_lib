package junit.framework

/**
 * A *Test* can be run and collect its results.

 * @see TestResult
 */
interface Test {
    /**
     * Counts the number of test cases that will be run by this test.
     */
    fun countTestCases(): Int

    /**
     * Runs a test and collects its result in a TestResult instance.
     */
    fun run(result: TestResult)
}