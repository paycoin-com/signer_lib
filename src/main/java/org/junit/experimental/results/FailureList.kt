package org.junit.experimental.results

import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

internal class FailureList(private val failures: List<Failure>) {

    fun result(): Result {
        val result = Result()
        val listener = result.createListener()
        for (failure in failures) {
            try {
                listener.testFailure(failure)
            } catch (e: Exception) {
                throw RuntimeException("I can't believe this happened")
            }

        }
        return result
    }
}