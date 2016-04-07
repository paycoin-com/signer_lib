package org.junit.runner.notification

/**
 * Thrown when a user has requested that the test run stop. Writers of
 * test running GUIs should be prepared to catch a `StoppedByUserException`.

 * @see org.junit.runner.notification.RunNotifier

 * @since 4.0
 */
class StoppedByUserException : RuntimeException() {
    companion object {
        private val serialVersionUID = 1L
    }
}
