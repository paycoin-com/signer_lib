package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.util.Arrays

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.InitializationError

class ErrorReportingRunner(private val testClass: Class<*>?, cause: Throwable) : Runner() {
    private val causes: List<Throwable>

    init {
        if (testClass == null) {
            throw NullPointerException("Test class cannot be null")
        }
        causes = getCauses(cause)
    }

    override val description: Description
        get() {
            val description = Description.createSuiteDescription(testClass)
            for (each in causes) {
                description.addChild(describeCause(each))
            }
            return description
        }

    override fun run(notifier: RunNotifier) {
        for (each in causes) {
            runCause(each, notifier)
        }
    }

    @SuppressWarnings("deprecation")
    private fun getCauses(cause: Throwable): List<Throwable> {
        if (cause is InvocationTargetException) {
            return getCauses(cause.cause)
        }
        if (cause is InitializationError) {
            return cause.causes
        }
        if (cause is org.junit.internal.runners.InitializationError) {
            return cause.causes
        }
        return Arrays.asList(cause)
    }

    private fun describeCause(child: Throwable): Description {
        return Description.createTestDescription(testClass,
                "initializationError")
    }

    private fun runCause(child: Throwable, notifier: RunNotifier) {
        val description = describeCause(child)
        notifier.fireTestStarted(description)
        notifier.fireTestFailure(Failure(description, child))
        notifier.fireTestFinished(description)
    }
}
