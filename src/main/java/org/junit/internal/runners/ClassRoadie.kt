package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import org.junit.internal.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class ClassRoadie(private val notifier: RunNotifier, private val testClass: TestClass,
                  private val description: Description, private val runnable: Runnable) {

    protected fun runUnprotected() {
        runnable.run()
    }

    protected fun addFailure(targetException: Throwable) {
        notifier.fireTestFailure(Failure(description, targetException))
    }

    fun runProtected() {
        try {
            runBefores()
            runUnprotected()
        } catch (e: FailedBefore) {
        } finally {
            runAfters()
        }
    }

    @Throws(FailedBefore::class)
    private fun runBefores() {
        try {
            try {
                val befores = testClass.befores
                for (before in befores) {
                    before.invoke(null)
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
        val afters = testClass.afters
        for (after in afters) {
            try {
                after.invoke(null)
            } catch (e: InvocationTargetException) {
                addFailure(e.targetException)
            } catch (e: Throwable) {
                addFailure(e) // Untested, but seems impossible
            }

        }
    }
}
