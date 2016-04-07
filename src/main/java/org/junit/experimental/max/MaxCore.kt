package org.junit.experimental.max

import java.io.File
import java.util.ArrayList
import java.util.Collections

import junit.framework.TestSuite
import org.junit.internal.requests.SortingRequest
import org.junit.internal.runners.ErrorReportingRunner
import org.junit.internal.runners.JUnit38ClassRunner
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.Runner
import org.junit.runners.Suite
import org.junit.runners.model.InitializationError

/**
 * A replacement for JUnitCore, which keeps track of runtime and failure history, and reorders tests
 * to maximize the chances that a failing test occurs early in the test run.

 * The rules for sorting are:
 *
 *  1.  Never-run tests first, in arbitrary order
 *  1.  Group remaining tests by the date at which they most recently failed.
 *  1.  Sort groups such that the most recent failure date is first, and never-failing tests are at the end.
 *  1.  Within a group, run the fastest tests first.
 *
 */
class MaxCore private constructor(storedResults: File) {

    private val history: MaxHistory

    init {
        history = MaxHistory.forFolder(storedResults)
    }

    /**
     * Run all the tests in `class`.

     * @return a [Result] describing the details of the test run and the failed tests.
     */
    fun run(testClass: Class<*>): Result {
        return run(Request.aClass(testClass))
    }

    /**
     * Run all the tests contained in `request`.

     * This variant should be used if `core` has attached listeners that this
     * run should notify.

     * @param request the request describing tests
     * *
     * @param core a JUnitCore to delegate to.
     * *
     * @return a [Result] describing the details of the test run and the failed tests.
     */
    @JvmOverloads fun run(request: Request, core: JUnitCore = JUnitCore()): Result {
        core.addListener(history.listener())
        return core.run(sortRequest(request).runner)
    }

    /**
     * @return a new Request, which contains all of the same tests, but in a new order.
     */
    fun sortRequest(request: Request): Request {
        if (request is SortingRequest) {
            // We'll pay big karma points for this
            return request
        }
        val leaves = findLeaves(request)
        Collections.sort(leaves, history.testComparator())
        return constructLeafRequest(leaves)
    }

    private fun constructLeafRequest(leaves: List<Description>): Request {
        val runners = ArrayList<Runner>()
        for (each in leaves) {
            runners.add(buildRunner(each))
        }
        return object : Request() {
            override val runner: Runner
                get() {
                    try {
                        return object : Suite(null as Class<*>, runners) {

                        }
                    } catch (e: InitializationError) {
                        return ErrorReportingRunner(null, e)
                    }

                }
        }
    }

    private fun buildRunner(each: Description): Runner {
        if (each.toString() == "TestSuite with 0 tests") {
            return Suite.emptySuite()
        }
        if (each.toString().startsWith(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX)) {
            // This is cheating, because it runs the whole class
            // to get the warning for this method, but we can't do better,
            // because JUnit 3.8's
            // thrown away which method the warning is for.
            return JUnit38ClassRunner(TestSuite(getMalformedTestClass(each)))
        }
        val type = each.testClass ?: throw RuntimeException("Can't build a runner from description [$each]")
        val methodName = each.methodName ?: return Request.aClass(type).runner
        return Request.method(type, methodName).runner
    }

    private fun getMalformedTestClass(each: Description): Class<*>? {
        try {
            return Class.forName(each.toString().replace(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX, ""))
        } catch (e: ClassNotFoundException) {
            return null
        }

    }

    /**
     * @param request a request to run
     * *
     * @return a list of method-level tests to run, sorted in the order
     * *         specified in the class comment.
     */
    fun sortedLeavesForTest(request: Request): List<Description> {
        return findLeaves(sortRequest(request))
    }

    private fun findLeaves(request: Request): List<Description> {
        val results = ArrayList<Description>()
        findLeaves(null, request.runner.description, results)
        return results
    }

    private fun findLeaves(parent: Description?, description: Description, results: MutableList<Description>) {
        if (description.children.isEmpty()) {
            if (description.toString() == "warning(junit.framework.TestSuite$1)") {
                results.add(Description.createSuiteDescription(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX + parent!!))
            } else {
                results.add(description)
            }
        } else {
            for (each in description.children) {
                findLeaves(description, each, results)
            }
        }
    }

    companion object {
        private val MALFORMED_JUNIT_3_TEST_CLASS_PREFIX = "malformed JUnit 3 test class: "

        /**
         * Create a new MaxCore from a serialized file stored at storedResults

         */
        @Deprecated("")
        @Deprecated("use storedLocally()")
        fun forFolder(folderName: String): MaxCore {
            return storedLocally(File(folderName))
        }

        /**
         * Create a new MaxCore from a serialized file stored at storedResults
         */
        fun storedLocally(storedResults: File): MaxCore {
            return MaxCore(storedResults)
        }
    }
}
/**
 * Run all the tests contained in `request`.

 * @param request the request describing tests
 * *
 * @return a [Result] describing the details of the test run and the failed tests.
 */