package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Collections
import java.util.Comparator

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.NoTestsRemainException
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class JUnit4ClassRunner @Throws(InitializationError::class)
constructor(klass: Class<*>) : Runner(), Filterable, Sortable {
    private val testMethods: List<Method>
    protected val testClass: TestClass

    init {
        testClass = TestClass(klass)
        testMethods = getTestMethods()
        validate()
    }

    protected fun getTestMethods(): List<Method> {
        return testClass.testMethods
    }

    @Throws(InitializationError::class)
    protected fun validate() {
        val methodValidator = MethodValidator(testClass)
        methodValidator.validateMethodsForDefaultRunner()
        methodValidator.assertValid()
    }

    override fun run(notifier: RunNotifier) {
        ClassRoadie(notifier, testClass, description, Runnable { runMethods(notifier) }).runProtected()
    }

    protected fun runMethods(notifier: RunNotifier) {
        for (method in testMethods) {
            invokeTestMethod(method, notifier)
        }
    }

    override val description: Description
        get() {
            val spec = Description.createSuiteDescription(name, *classAnnotations())
            val testMethods = this.testMethods
            for (method in testMethods) {
                spec.addChild(methodDescription(method))
            }
            return spec
        }

    protected fun classAnnotations(): Array<Annotation> {
        return testClass.javaClass.annotations
    }

    protected val name: String
        get() = testClass.name

    @Throws(Exception::class)
    protected fun createTest(): Any {
        return testClass.constructor.newInstance()
    }

    protected fun invokeTestMethod(method: Method, notifier: RunNotifier) {
        val description = methodDescription(method)
        val test: Any
        try {
            test = createTest()
        } catch (e: InvocationTargetException) {
            testAborted(notifier, description, e.cause)
            return
        } catch (e: Exception) {
            testAborted(notifier, description, e)
            return
        }

        val testMethod = wrapMethod(method)
        MethodRoadie(test, testMethod, notifier, description).run()
    }

    private fun testAborted(notifier: RunNotifier, description: Description,
                            e: Throwable) {
        notifier.fireTestStarted(description)
        notifier.fireTestFailure(Failure(description, e))
        notifier.fireTestFinished(description)
    }

    protected fun wrapMethod(method: Method): TestMethod {
        return TestMethod(method, testClass)
    }

    protected fun testName(method: Method): String {
        return method.name
    }

    protected fun methodDescription(method: Method): Description {
        return Description.createTestDescription(testClass.javaClass, testName(method), *testAnnotations(method))
    }

    protected fun testAnnotations(method: Method): Array<Annotation> {
        return method.annotations
    }

    @Throws(NoTestsRemainException::class)
    override fun filter(filter: Filter) {
        val iter = testMethods.iterator()
        while (iter.hasNext()) {
            val method = iter.next()
            if (!filter.shouldRun(methodDescription(method))) {
                iter.remove()
            }
        }
        if (testMethods.isEmpty()) {
            throw NoTestsRemainException()
        }
    }

    override fun sort(sorter: Sorter) {
        Collections.sort(testMethods) { o1, o2 -> sorter.compare(methodDescription(o1), methodDescription(o2)) }
    }
}