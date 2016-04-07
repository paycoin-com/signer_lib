package org.junit.runner.manipulation

/**
 * Thrown when a filter removes all tests from a runner.

 * @since 4.0
 */
class NoTestsRemainException : Exception() {
    companion object {
        private val serialVersionUID = 1L
    }
}
