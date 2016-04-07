package junit.framework

/**
 * Thrown when an assertion failed.
 */
open class AssertionFailedError : AssertionError {

    /**
     * Constructs a new AssertionFailedError without a detail message.
     */
    constructor() {
    }

    /**
     * Constructs a new AssertionFailedError with the specified detail message.
     * A null message is replaced by an empty String.
     * @param message the detail message. The detail message is saved for later
     * * retrieval by the `Throwable.getMessage()` method.
     */
    constructor(message: String) : super(defaultString(message)) {
    }

    companion object {

        private val serialVersionUID = 1L

        private fun defaultString(message: String?): String {
            return message ?: ""
        }
    }
}