package org.junit.experimental.max

/**
 * Thrown when Max cannot read the MaxCore serialization
 */
class CouldNotReadCoreException
/**
 * Constructs
 */
(e: Throwable) : Exception(e) {
    companion object {
        private val serialVersionUID = 1L
    }
}
