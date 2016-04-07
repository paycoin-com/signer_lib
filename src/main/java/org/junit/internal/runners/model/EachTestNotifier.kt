package org.junit.internal.runners.model

import org.junit.internal.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.MultipleFailureException

class EachTestNotifier(private val notifier: RunNotifier, private val description: Description) {

    fun addFailure(targetException: Throwable) {
        if (targetException is MultipleFailureException) {
            addMultipleFailureException(targetException)
        } else {
            notifier.fireTestFailure(Failure(description, targetException))
        }
    }

    private fun addMultipleFailureException(mfe: MultipleFailureException) {
        for (each in mfe.failures) {
            addFailure(each)
        }
    }

    fun addFailedAssumption(e: AssumptionViolatedException) {
        notifier.fireTestAssumptionFailed(Failure(description, e))
    }

    fun fireTestFinished() {
        notifier.fireTestFinished(description)
    }

    fun fireTestStarted() {
        notifier.fireTestStarted(description)
    }

    fun fireTestIgnored() {
        notifier.fireTestIgnored(description)
    }
}