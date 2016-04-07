package junit.framework

import java.io.PrintWriter
import java.io.StringWriter


/**
 * A `TestFailure` collects a failed test together with
 * the caught exception.

 * @see TestResult
 */
class TestFailure
/**
 * Constructs a TestFailure with the given test and exception.
 */
(protected var fFailedTest: Test, protected var fThrownException: Throwable) {

    /**
     * Gets the failed test.
     */
    fun failedTest(): Test {
        return fFailedTest
    }

    /**
     * Gets the thrown exception.
     */
    fun thrownException(): Throwable {
        return fThrownException
    }

    /**
     * Returns a short description of the failure.
     */
    override fun toString(): String {
        return fFailedTest + ": " + fThrownException.message
    }

    /**
     * Returns a String containing the stack trace of the error
     * thrown by TestFailure.
     */
    fun trace(): String {
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        thrownException().printStackTrace(writer)
        return stringWriter.toString()
    }

    /**
     * Returns a String containing the message from the thrown exception.
     */
    fun exceptionMessage(): String {
        return thrownException().message
    }

    /**
     * Returns `true` if the error is considered a failure
     * (i.e. if it is an instance of `AssertionFailedError`),
     * `false` otherwise.
     */
    val isFailure: Boolean
        get() = thrownException() is AssertionFailedError
}