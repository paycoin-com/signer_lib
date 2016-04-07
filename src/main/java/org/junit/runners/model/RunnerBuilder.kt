package org.junit.runners.model

import java.util.ArrayList
import java.util.HashSet

import org.junit.internal.runners.ErrorReportingRunner
import org.junit.runner.Runner

/**
 * A RunnerBuilder is a strategy for constructing runners for classes.

 * Only writers of custom runners should use `RunnerBuilder`s.  A custom runner class with a constructor taking
 * a `RunnerBuilder` parameter will be passed the instance of `RunnerBuilder` used to build that runner itself.
 * For example,
 * imagine a custom runner that builds suites based on a list of classes in a text file:

 *
 * \@RunWith(TextFileSuite.class)
 * \@SuiteSpecFile("mysuite.txt")
 * class MySuite {}
 *

 * The implementation of TextFileSuite might include:

 *
 * public TextFileSuite(Class testClass, RunnerBuilder builder) {
 * // ...
 * for (String className : readClassNames())
 * addRunner(builder.runnerForClass(Class.forName(className)));
 * // ...
 * }
 *

 * @see org.junit.runners.Suite

 * @since 4.5
 */
abstract class RunnerBuilder {
    private val parents = HashSet<Class<*>>()

    /**
     * Override to calculate the correct runner for a test class at runtime.

     * @param testClass class to be run
     * *
     * @return a Runner
     * *
     * @throws Throwable if a runner cannot be constructed
     */
    @Throws(Throwable::class)
    abstract fun runnerForClass(testClass: Class<*>): Runner

    /**
     * Always returns a runner, even if it is just one that prints an error instead of running tests.

     * @param testClass class to be run
     * *
     * @return a Runner
     */
    fun safeRunnerForClass(testClass: Class<*>): Runner? {
        try {
            return runnerForClass(testClass)
        } catch (e: Throwable) {
            return ErrorReportingRunner(testClass, e)
        }

    }

    @Throws(InitializationError::class)
    internal fun addParent(parent: Class<*>): Class<*> {
        if (!parents.add(parent)) {
            throw InitializationError(String.format("class '%s' (possibly indirectly) contains itself as a SuiteClass", parent.name))
        }
        return parent
    }

    internal fun removeParent(klass: Class<*>) {
        parents.remove(klass)
    }

    /**
     * Constructs and returns a list of Runners, one for each child class in
     * `children`.  Care is taken to avoid infinite recursion:
     * this builder will throw an exception if it is requested for another
     * runner for `parent` before this call completes.
     */
    @Throws(InitializationError::class)
    fun runners(parent: Class<*>, children: Array<Class<*>>): List<Runner> {
        addParent(parent)

        try {
            return runners(children)
        } finally {
            removeParent(parent)
        }
    }

    @Throws(InitializationError::class)
    fun runners(parent: Class<*>, children: List<Class<*>>): List<Runner> {
        return runners(parent, children.toArray<Class<*>>(arrayOfNulls<Class<*>>(0)))
    }

    private fun runners(children: Array<Class<*>>): List<Runner> {
        val runners = ArrayList<Runner>()
        for (each in children) {
            val childRunner = safeRunnerForClass(each)
            if (childRunner != null) {
                runners.add(childRunner)
            }
        }
        return runners
    }
}
