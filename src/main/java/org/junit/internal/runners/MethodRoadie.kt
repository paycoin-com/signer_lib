package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.junit.internal.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.TestTimedOutException


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class MethodRoadie(private val test: Any, private val testMethod: TestMethod, private val notifier: RunNotifier, private val description: Description) {

    fun run() {
        if (testMethod.isIgnored) {
            notifier.fireTestIgnored(description)
            return
        }
        notifier.fireTestStarted(description)
        try {
            val timeout = testMethod.timeout
            if (timeout > 0) {
                runWithTimeout(timeout)
            } else {
                runTest()
            }
        } finally {
            notifier.fireTestFinished(description)
        }
    }

    private fun runWithTimeout(timeout: Long) {
        runBeforesThenTestThenAfters(Runnable {
            val service = Executors.newSingleThreadExecutor()
            val callable = Callable<kotlin.Any> {
                runTestMethod()
                null
            }
            val result = service.submit(callable)
            service.shutdown()
            try {
                val terminated = service.awaitTermination(timeout,
                        TimeUnit.MILLISECONDS)
                if (!terminated) {
                    service.shutdownNow()
                }
                result.get(0, TimeUnit.MILLISECONDS) // throws the exception if one occurred during the invocation
            } catch (e: TimeoutException) {
                addFailure(TestTimedOutException(timeout, TimeUnit.MILLISECONDS))
            } catch (e: Exception) {
                addFailure(e)
            }
        })
    }

    fun runTest() {
        runBeforesThenTestThenAfters(Runnable { runTestMethod() })
    }

    fun runBeforesThenTestThenAfters(test: Runnable) {
        try {
            runBefores()
            test.run()
        } catch (e: FailedBefore) {
        } catch (e: Exception) {
            throw RuntimeException("test should never throw an exception to this level")
        } finally {
            runAfters()
        }
    }

    protected fun runTestMethod() {
        try {
            testMethod.invoke(test)
            if (testMethod.expectsException()) {
                addFailure(AssertionError("Expected exception: " + testMethod.expectedException!!.name))
            }
        } catch (e: InvocationTargetException) {
            val actual = e.targetException
            if (actual is AssumptionViolatedException) {
                return
            } else if (!testMethod.expectsException()) {
                addFailure(actual)
            } else if (testMethod.isUnexpected(actual)) {
                val message = "Unexpected exception, expected<" + testMethod.expectedException!!.name + "> but was<"
                +actual.javaClass.name + ">"
                addFailure(Exception(message, actual))
            }
        } catch (e: Throwable) {
            addFailure(e)
        }

    }

    @Throws(FailedBefore::class)
    private fun runBefores() {
        try {
            try {
                val befores = testMethod.befores
                for (before in befores) {
                    before.invoke(test)
                }
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }

        } catch (e: AssumptionViolatedException) {
            throw FailedBefore()
        } catch (e: Throwable) {
            addFailure(e)
            throw FailedBefore()
        }

    }

    private fun runAfters() {
        val afters = testMethod.afters
        for (after in afters) {
            try {
                after.invoke(test)
            } catch (e: InvocationTargetException) {
                addFailure(e.targetException)
            } catch (e: Throwable) {
                addFailure(e) // Untested, but seems impossible
            }

        }
    }

    protected fun addFailure(e: Throwable) {
        notifier.fireTestFailure(Failure(description, e))
    }
}

