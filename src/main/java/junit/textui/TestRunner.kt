package junit.textui


import java.io.PrintStream

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestResult
import junit.framework.TestSuite
import junit.runner.BaseTestRunner
import junit.runner.Version

/**
 * A command line based tool to run tests.
 *
 * java junit.textui.TestRunner [-wait] TestCaseClass
 *
 *
 *
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static `suite` method it
 * will be invoked and the returned test is run. Otherwise all
 * the methods starting with "test" having no arguments are run.
 *
 *
 * When the wait command line argument is given TestRunner
 * waits until the users types RETURN.
 *
 *
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end.
 */
class TestRunner
/**
 * Constructs a TestRunner using the given ResultPrinter all the output
 */
(private var fPrinter: ResultPrinter?) : BaseTestRunner() {

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    @JvmOverloads constructor(writer: PrintStream = System.out) : this(ResultPrinter(writer)) {
    }

    override fun testFailed(status: Int, test: Test, e: Throwable) {
    }

    override fun testStarted(testName: String) {
    }

    override fun testEnded(testName: String) {
    }

    /**
     * Creates the TestResult to be used for the test run.
     */
    protected fun createTestResult(): TestResult {
        return TestResult()
    }

    fun doRun(test: Test): TestResult {
        return doRun(test, false)
    }

    fun doRun(suite: Test, wait: Boolean): TestResult {
        val result = createTestResult()
        result.addListener(fPrinter)
        val startTime = System.currentTimeMillis()
        suite.run(result)
        val endTime = System.currentTimeMillis()
        val runTime = endTime - startTime
        fPrinter!!.print(result, runTime)

        pause(wait)
        return result
    }

    protected fun pause(wait: Boolean) {
        if (!wait) return
        fPrinter!!.printWaitPrompt()
        try {
            System.`in`.read()
        } catch (e: Exception) {
        }

    }

    /**
     * Starts a test run. Analyzes the command line arguments and runs the given
     * test suite.
     */
    @Throws(Exception::class)
    fun start(args: Array<String>): TestResult {
        var testCase = ""
        var method = ""
        var wait = false

        var i = 0
        while (i < args.size) {
            if (args[i] == "-wait") {
                wait = true
            } else if (args[i] == "-c") {
                testCase = extractClassName(args[++i])
            } else if (args[i] == "-m") {
                val arg = args[++i]
                val lastIndex = arg.lastIndexOf('.')
                testCase = arg.substring(0, lastIndex)
                method = arg.substring(lastIndex + 1)
            } else if (args[i] == "-v") {
                System.err.println("JUnit " + Version.id() + " by Kent Beck and Erich Gamma")
            } else {
                testCase = args[i]
            }
            i++
        }

        if (testCase == "") {
            throw Exception("Usage: TestRunner [-wait] testCaseName, where name is the name of the TestCase class")
        }

        try {
            if (method != "") {
                return runSingleMethod(testCase, method, wait)
            }
            val suite = getTest(testCase)
            return doRun(suite, wait)
        } catch (e: Exception) {
            throw Exception("Could not create and run test suite: " + e)
        }

    }

    @Throws(Exception::class)
    protected fun runSingleMethod(testCase: String, method: String, wait: Boolean): TestResult {
        val testClass = loadSuiteClass(testCase).asSubclass(TestCase::class.java)
        val test = TestSuite.createTest(testClass, method)
        return doRun(test, wait)
    }

    override fun runFailed(message: String) {
        System.err.println(message)
        System.exit(FAILURE_EXIT)
    }

    fun setPrinter(printer: ResultPrinter) {
        fPrinter = printer
    }

    companion object {

        val SUCCESS_EXIT = 0
        val FAILURE_EXIT = 1
        val EXCEPTION_EXIT = 2

        /**
         * Runs a suite extracted from a TestCase subclass.
         */
        fun run(testClass: Class<out TestCase>) {
            run(TestSuite(testClass))
        }

        /**
         * Runs a single test and collects its results.
         * This method can be used to start a test run
         * from your program.
         *
         * public static void main (String[] args) {
         * test.textui.TestRunner.run(suite());
         * }
         *
         */
        fun run(test: Test): TestResult {
            val runner = TestRunner()
            return runner.doRun(test)
        }

        /**
         * Runs a single test and waits until the user
         * types RETURN.
         */
        fun runAndWait(suite: Test) {
            val aTestRunner = TestRunner()
            aTestRunner.doRun(suite, true)
        }

        @JvmStatic fun main(args: Array<String>) {
            val aTestRunner = TestRunner()
            try {
                val r = aTestRunner.start(args)
                if (!r.wasSuccessful()) {
                    System.exit(FAILURE_EXIT)
                }
                System.exit(SUCCESS_EXIT)
            } catch (e: Exception) {
                System.err.println(e.message)
                System.exit(EXCEPTION_EXIT)
            }

        }
    }


}
/**
 * Constructs a TestRunner.
 */