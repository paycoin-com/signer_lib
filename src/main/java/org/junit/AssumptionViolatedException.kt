package org.junit

import org.hamcrest.Matcher

/**
 * An exception class used to implement *assumptions* (state in which a given test
 * is meaningful and should or should not be executed). A test for which an assumption
 * fails should not generate a test case failure.

 * @see org.junit.Assume

 * @since 4.12
 */
@SuppressWarnings("deprecation")
class AssumptionViolatedException : org.junit.internal.AssumptionViolatedException {

    /**
     * An assumption exception with the given *actual* value and a *matcher* describing
     * the expectation that failed.
     */
    constructor(actual: T, matcher: Matcher<T>) : super(actual, matcher) {
    }

    /**
     * An assumption exception with a message with the given *actual* value and a
     * *matcher* describing the expectation that failed.
     */
    constructor(message: String, expected: T, matcher: Matcher<T>) : super(message, expected, matcher) {
    }

    /**
     * An assumption exception with the given message only.
     */
    constructor(message: String) : super(message) {
    }

    /**
     * An assumption exception with the given message and a cause.
     */
    constructor(assumption: String, t: Throwable) : super(assumption, t) {
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
