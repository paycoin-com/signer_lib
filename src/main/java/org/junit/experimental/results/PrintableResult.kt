package org.junit.experimental.results

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import org.junit.internal.TextListener
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.notification.Failure

/**
 * A test result that prints nicely in error messages.
 * This is only intended to be used in JUnit self-tests.
 * For example:

 *
 * assertThat(testResult(HasExpectedException.class), isSuccessful());
 *
 */
class PrintableResult private constructor(private val result: Result) {

    /**
     * A result that includes the given `failures`
     */
    constructor(failures: List<Failure>) : this(FailureList(failures).result()) {
    }

    /**
     * Returns the number of failures in this result.
     */
    fun failureCount(): Int {
        return result.failures.size
    }

    override fun toString(): String {
        val stream = ByteArrayOutputStream()
        TextListener(PrintStream(stream)).testRunFinished(result)
        return stream.toString()
    }

    companion object {

        /**
         * The result of running JUnit on `type`
         */
        fun testResult(type: Class<*>): PrintableResult {
            return testResult(Request.aClass(type))
        }

        /**
         * The result of running JUnit on Request `request`
         */
        fun testResult(request: Request): PrintableResult {
            return PrintableResult(JUnitCore().run(request))
        }
    }
}