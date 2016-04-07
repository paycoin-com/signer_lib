package org.junit.runner

import junit.runner.Version
import org.junit.internal.JUnitSystem
import org.junit.internal.RealSystem
import org.junit.internal.TextListener
import org.junit.internal.runners.JUnit38ClassRunner
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier

/**
 * `JUnitCore` is a facade for running tests. It supports running JUnit 4 tests,
 * JUnit 3.8.x tests, and mixtures. To run tests from the command line, run
 * `java org.junit.runner.JUnitCore TestClass1 TestClass2 ...`.
 * For one-shot test runs, use the static method [.runClasses].
 * If you want to add special listeners,
 * create an instance of [org.junit.runner.JUnitCore] first and use it to run the tests.

 * @see org.junit.runner.Result

 * @see org.junit.runner.notification.RunListener

 * @see org.junit.runner.Request

 * @since 4.0
 */
class JUnitCore {
    private val notifier = RunNotifier()

    /**
     * @param system
     * *
     * @param args from main()
     */
    internal fun runMain(system: JUnitSystem, vararg args: String): Result {
        system.out().println("JUnit version " + Version.id())

        val jUnitCommandLineParseResult = JUnitCommandLineParseResult.parse(args)

        val listener = TextListener(system)
        addListener(listener)

        return run(jUnitCommandLineParseResult.createRequest(defaultComputer()))
    }

    /**
     * @return the version number of this release
     */
    val version: String
        get() = Version.id()

    /**
     * Run all the tests in `classes`.

     * @param classes the classes containing tests
     * *
     * @return a [Result] describing the details of the test run and the failed tests.
     */
    fun run(vararg classes: Class<*>): Result {
        return run(defaultComputer(), *classes)
    }

    /**
     * Run all the tests in `classes`.

     * @param computer Helps construct Runners from classes
     * *
     * @param classes the classes containing tests
     * *
     * @return a [Result] describing the details of the test run and the failed tests.
     */
    fun run(computer: Computer, vararg classes: Class<*>): Result {
        return run(Request.classes(computer, *classes))
    }

    /**
     * Run all the tests contained in `request`.

     * @param request the request describing tests
     * *
     * @return a [Result] describing the details of the test run and the failed tests.
     */
    fun run(request: Request): Result {
        return run(request.runner)
    }

    /**
     * Run all the tests contained in JUnit 3.8.x `test`. Here for backward compatibility.

     * @param test the old-style test
     * *
     * @return a [Result] describing the details of the test run and the failed tests.
     */
    fun run(test: junit.framework.Test): Result {
        return run(JUnit38ClassRunner(test))
    }

    /**
     * Do not use. Testing purposes only.
     */
    fun run(runner: Runner): Result {
        val result = Result()
        val listener = result.createListener()
        notifier.addFirstListener(listener)
        try {
            notifier.fireTestRunStarted(runner.description)
            runner.run(notifier)
            notifier.fireTestRunFinished(result)
        } finally {
            removeListener(listener)
        }
        return result
    }

    /**
     * Add a listener to be notified as the tests run.

     * @param listener the listener to add
     * *
     * @see org.junit.runner.notification.RunListener
     */
    fun addListener(listener: RunListener) {
        notifier.addListener(listener)
    }

    /**
     * Remove a listener.

     * @param listener the listener to remove
     */
    fun removeListener(listener: RunListener) {
        notifier.removeListener(listener)
    }

    companion object {

        /**
         * Run the tests contained in the classes named in the `args`.
         * If all tests run successfully, exit with a status of 0. Otherwise exit with a status of 1.
         * Write feedback while tests are running and write
         * stack traces for all failed tests after the tests all complete.

         * @param args names of classes in which to find tests to run
         */
        @JvmStatic fun main(args: Array<String>) {
            val result = JUnitCore().runMain(RealSystem(), *args)
            System.exit(if (result.wasSuccessful()) 0 else 1)
        }

        /**
         * Run the tests contained in `classes`. Write feedback while the tests
         * are running and write stack traces for all failed tests after all tests complete. This is
         * similar to [.main], but intended to be used programmatically.

         * @param classes Classes in which to find tests
         * *
         * @return a [Result] describing the details of the test run and the failed tests.
         */
        fun runClasses(vararg classes: Class<*>): Result {
            return runClasses(defaultComputer(), *classes)
        }

        /**
         * Run the tests contained in `classes`. Write feedback while the tests
         * are running and write stack traces for all failed tests after all tests complete. This is
         * similar to [.main], but intended to be used programmatically.

         * @param computer Helps construct Runners from classes
         * *
         * @param classes  Classes in which to find tests
         * *
         * @return a [Result] describing the details of the test run and the failed tests.
         */
        fun runClasses(computer: Computer, vararg classes: Class<*>): Result {
            return JUnitCore().run(computer, *classes)
        }

        internal fun defaultComputer(): Computer {
            return Computer()
        }
    }
}
