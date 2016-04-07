package org.junit.runners.model


/**
 * Represents one or more actions to be taken at runtime in the course
 * of running a JUnit test suite.

 * @since 4.5
 */
abstract class Statement {
    /**
     * Run the action, throwing a `Throwable` if anything goes wrong.
     */
    @Throws(Throwable::class)
    abstract fun evaluate()
}