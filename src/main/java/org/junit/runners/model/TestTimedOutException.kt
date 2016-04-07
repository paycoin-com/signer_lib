package org.junit.runners.model

import java.util.concurrent.TimeUnit

/**
 * Exception thrown when a test fails on timeout.

 * @since 4.12
 */
class TestTimedOutException
/**
 * Creates exception with a standard message "test timed out after [timeout] [timeUnit]"

 * @param timeout the amount of time passed before the test was interrupted
 * *
 * @param timeUnit the time unit for the timeout value
 */
(
        /**
         * Gets the time passed before the test was interrupted
         */
        val timeout: Long,
        /**
         * Gets the time unit for the timeout value
         */
        val timeUnit: TimeUnit) : Exception(String.format("test timed out after %d %s",
        timeout, timeUnit.name.toLowerCase())) {
    companion object {

        private val serialVersionUID = 31935685163547539L
    }
}
