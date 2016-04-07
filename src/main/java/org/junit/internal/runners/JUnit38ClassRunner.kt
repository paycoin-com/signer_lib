package org.junit.internal.runners

import junit.extensions.TestDecorator
import junit.framework.AssertionFailedError
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestListener
import junit.framework.TestResult
import junit.framework.TestSuite
import org.junit.runner.Describable
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.NoTestsRemainException
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import java.lang.reflect.Method

open class JUnit38ClassRunner(test: Test) : Runner(), Filterable, Sortable {
    private class OldTestClassAdaptingListener private constructor(private val notifier: RunNotifier) : TestListener {

        override fun endTest(test: Test) {
            notifier.fireTestFinished(asDescription(test))
        }

        override fun startTest(test: Test) {
            notifier.fireTestStarted(asDescription(test))
        }

        // Implement junit.framework.TestListener
        override fun addError(test: Test, e: Throwable) {
            val failure = Failure(asDescription(test), e)
            notifier.fireTestFailure(failure)
        }

        private fun asDescription(test: Test): Description {
            if (test is Describable) {
                return test.description
            }
            return Description.createTestDescription(getEffectiveClass(test), getName(test))
        }

        private fun getEffectiveClass(test: Test): Class<out Test> {
            return test.javaClass
        }

        private fun getName(test: Test): String {
            if (test is TestCase) {
                return test.name
            } else {
                return test.toString()
            }
        }

        override fun addFailure(test: Test, t: AssertionFailedError) {
            addError(test, t)
        }
    }

    @Volatile private var test: Test? = null

    constructor(klass: Class<*>) : this(TestSuite(klass.asSubclass(TestCase::class.java))) {
    }

    init {
        test = test
    }

    override fun run(notifier: RunNotifier) {
        val result = TestResult()
        result.addListener(createAdaptingListener(notifier))
        test.run(result)
    }

    fun createAdaptingListener(notifier: RunNotifier): TestListener {
        return OldTestClassAdaptingListener(notifier)
    }

    override val description: Description
        get() = makeDescription(test)

    private fun makeDescription(test: Test): Description {
        if (test is TestCase) {
            return Description.createTestDescription(test.javaClass, test.name,
                    *getAnnotations(test))
        } else if (test is TestSuite) {
            val name = if (test.name == null) createSuiteDescription(test) else test.name
            val description = Description.createSuiteDescription(name)
            val n = test.testCount()
            for (i in 0..n - 1) {
                val made = makeDescription(test.testAt(i))
                description.addChild(made)
            }
            return description
        } else if (test is Describable) {
            return test.description
        } else if (test is TestDecorator) {
            return makeDescription(test.test)
        } else {
            // This is the best we can do in this case
            return Description.createSuiteDescription(test.javaClass)
        }
    }

    /**
     * Get the annotations associated with given TestCase.
     * @param test the TestCase.
     */
    private fun getAnnotations(test: TestCase): Array<Annotation> {
        try {
            val m = test.javaClass.getMethod(test.name)
            return m.declaredAnnotations
        } catch (e: SecurityException) {
        } catch (e: NoSuchMethodException) {
        }

        return arrayOfNulls(0)
    }

    private fun createSuiteDescription(ts: TestSuite): String {
        val count = ts.countTestCases()
        val example = if (count == 0) "" else String.format(" [example: %s]", ts.testAt(0))
        return String.format("TestSuite with %s tests%s", count, example)
    }

    @Throws(NoTestsRemainException::class)
    override fun filter(filter: Filter) {
        if (test is Filterable) {
            val adapter = test as Filterable
            adapter.filter(filter)
        } else if (test is TestSuite) {
            val suite = test as TestSuite
            val filtered = TestSuite(suite.name)
            val n = suite.testCount()
            for (i in 0..n - 1) {
                val test = suite.testAt(i)
                if (filter.shouldRun(makeDescription(test))) {
                    filtered.addTest(test)
                }
            }
            test = filtered
            if (filtered.testCount() == 0) {
                throw NoTestsRemainException()
            }
        }
    }

    override fun sort(sorter: Sorter) {
        if (test is Sortable) {
            val adapter = test as Sortable
            adapter.sort(sorter)
        }
    }
}
