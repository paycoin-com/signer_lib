package org.junit.runner.notification

import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter

import org.junit.runner.Description

/**
 * A `Failure` holds a description of the failed test and the
 * exception that was thrown while running it. In most cases the [org.junit.runner.Description]
 * will be of a single test. However, if problems are encountered while constructing the
 * test (for example, if a [org.junit.BeforeClass] method is not static), it may describe
 * something other than a single test.

 * @since 4.0
 */
class Failure
/**
 * Constructs a `Failure` with the given description and exception.

 * @param description a [org.junit.runner.Description] of the test that failed
 * *
 * @param thrownException the exception that was thrown while running the test
 */
(/*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
        /**
         * @return the raw description of the context of the failure.
         */
        val description: Description,
        /**
         * @return the exception thrown
         */

        val exception: Throwable) : Serializable {

    /**
     * @return a user-understandable label for the test
     */
    val testHeader: String
        get() = description.displayName

    override fun toString(): String {
        return testHeader + ": " + exception.message
    }

    /**
     * Convenience method

     * @return the printed form of the exception
     */
    val trace: String
        get() {
            val stringWriter = StringWriter()
            val writer = PrintWriter(stringWriter)
            exception.printStackTrace(writer)
            return stringWriter.toString()
        }

    /**
     * Convenience method

     * @return the message of the thrown exception
     */
    val message: String
        get() = exception.message

    companion object {
        private val serialVersionUID = 1L
    }
}
