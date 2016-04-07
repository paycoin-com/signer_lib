package org.junit.runner

import java.util.Comparator

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder
import org.junit.internal.requests.ClassRequest
import org.junit.internal.requests.FilterRequest
import org.junit.internal.requests.SortingRequest
import org.junit.internal.runners.ErrorReportingRunner
import org.junit.runner.manipulation.Filter
import org.junit.runners.model.InitializationError

/**
 * A `Request` is an abstract description of tests to be run. Older versions of
 * JUnit did not need such a concept--tests to be run were described either by classes containing
 * tests or a tree of [org.junit.Test]s. However, we want to support filtering and sorting,
 * so we need a more abstract specification than the tests themselves and a richer
 * specification than just the classes.

 *
 * The flow when JUnit runs tests is that a `Request` specifies some tests to be run -&gt;
 * a [org.junit.runner.Runner] is created for each class implied by the `Request` -&gt;
 * the [org.junit.runner.Runner] returns a detailed [org.junit.runner.Description]
 * which is a tree structure of the tests to be run.

 * @since 4.0
 */
abstract class Request {

    /**
     * Returns a [Runner] for this Request

     * @return corresponding [Runner] for this Request
     */
    abstract val runner: Runner

    /**
     * Returns a Request that only contains those tests that should run when
     * `filter` is applied

     * @param filter The [Filter] to apply to this Request
     * *
     * @return the filtered Request
     */
    fun filterWith(filter: Filter): Request {
        return FilterRequest(this, filter)
    }

    /**
     * Returns a Request that only runs contains tests whose [Description]
     * equals `desiredDescription`

     * @param desiredDescription [Description] of those tests that should be run
     * *
     * @return the filtered Request
     */
    fun filterWith(desiredDescription: Description): Request {
        return filterWith(Filter.matchMethodDescription(desiredDescription))
    }

    /**
     * Returns a Request whose Tests can be run in a certain order, defined by
     * `comparator`
     *
     *
     * For example, here is code to run a test suite in alphabetical order:
     *
     * private static Comparator&lt;Description&gt; forward() {
     * return new Comparator&lt;Description&gt;() {
     * public int compare(Description o1, Description o2) {
     * return o1.getDisplayName().compareTo(o2.getDisplayName());
     * }
     * };
     * }

     * public static main() {
     * new JUnitCore().run(Request.aClass(AllTests.class).sortWith(forward()));
     * }
     *

     * @param comparator definition of the order of the tests in this Request
     * *
     * @return a Request with ordered Tests
     */
    fun sortWith(comparator: Comparator<Description>): Request {
        return SortingRequest(this, comparator)
    }

    companion object {
        /**
         * Create a `Request` that, when processed, will run a single test.
         * This is done by filtering out all other tests. This method is used to support rerunning
         * single tests.

         * @param clazz the class of the test
         * *
         * @param methodName the name of the test
         * *
         * @return a `Request` that will cause a single test be run
         */
        fun method(clazz: Class<*>, methodName: String): Request {
            val method = Description.createTestDescription(clazz, methodName)
            return Request.aClass(clazz).filterWith(method)
        }

        /**
         * Create a `Request` that, when processed, will run all the tests
         * in a class. The odd name is necessary because `class` is a reserved word.

         * @param clazz the class containing the tests
         * *
         * @return a `Request` that will cause all tests in the class to be run
         */
        fun aClass(clazz: Class<*>): Request {
            return ClassRequest(clazz)
        }

        /**
         * Create a `Request` that, when processed, will run all the tests
         * in a class. If the class has a suite() method, it will be ignored.

         * @param clazz the class containing the tests
         * *
         * @return a `Request` that will cause all tests in the class to be run
         */
        fun classWithoutSuiteMethod(clazz: Class<*>): Request {
            return ClassRequest(clazz, false)
        }

        /**
         * Create a `Request` that, when processed, will run all the tests
         * in a set of classes.

         * @param computer Helps construct Runners from classes
         * *
         * @param classes the classes containing the tests
         * *
         * @return a `Request` that will cause all tests in the classes to be run
         */
        fun classes(computer: Computer, vararg classes: Class<*>): Request {
            try {
                val builder = AllDefaultPossibilitiesBuilder(true)
                val suite = computer.getSuite(builder, classes)
                return runner(suite)
            } catch (e: InitializationError) {
                throw RuntimeException(
                        "Bug in saff's brain: Suite constructor, called as above, should always complete")
            }

        }

        /**
         * Create a `Request` that, when processed, will run all the tests
         * in a set of classes with the default `Computer`.

         * @param classes the classes containing the tests
         * *
         * @return a `Request` that will cause all tests in the classes to be run
         */
        fun classes(vararg classes: Class<*>): Request {
            return classes(JUnitCore.defaultComputer(), *classes)
        }


        /**
         * Creates a [Request] that, when processed, will report an error for the given
         * test class with the given cause.
         */
        fun errorReport(klass: Class<*>, cause: Throwable): Request {
            return runner(ErrorReportingRunner(klass, cause))
        }

        /**
         * @param runner the runner to return
         * *
         * @return a `Request` that will run the given runner when invoked
         */
        fun runner(runner: Runner): Request {
            return object : Request() {
                override val runner: Runner
                    get() = runner
            }
        }
    }
}
