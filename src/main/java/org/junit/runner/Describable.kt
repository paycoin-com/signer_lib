package org.junit.runner


/**
 * Represents an object that can describe itself

 * @since 4.5
 */
interface Describable {
    /**
     * @return a [Description] showing the tests to be run by the receiver
     */
    val description: Description
}