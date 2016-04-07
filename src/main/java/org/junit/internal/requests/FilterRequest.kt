package org.junit.internal.requests

import org.junit.internal.runners.ErrorReportingRunner
import org.junit.runner.Request
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.NoTestsRemainException

/**
 * A filtered [Request].
 */
class FilterRequest
/**
 * Creates a filtered Request

 * @param request a [Request] describing your Tests
 * *
 * @param filter [Filter] to apply to the Tests described in
 * * `request`
 */
(private val request: Request, /*
     * We have to use the f prefix, because IntelliJ's JUnit4IdeaTestRunner uses
     * reflection to access this field. See
     * https://github.com/junit-team/junit/issues/960
     */
 private val fFilter: Filter) : Request() {

    override val runner: Runner
        get() {
            try {
                val runner = request.runner
                fFilter.apply(runner)
                return runner
            } catch (e: NoTestsRemainException) {
                return ErrorReportingRunner(Filter::class.java, Exception(String.format("No tests found matching %s from %s", fFilter.describe(), request.toString())))
            }

        }
}