package org.junit.internal

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.SelfDescribing
import org.hamcrest.StringDescription

/**
 * An exception class used to implement *assumptions* (state in which a given test
 * is meaningful and should or should not be executed). A test for which an assumption
 * fails should not generate a test case failure.

 * @see org.junit.Assume
 */
open class AssumptionViolatedException
@Deprecated("")
@Deprecated("Please use {@link org.junit.AssumptionViolatedException} instead.")
@JvmOverloads constructor(/*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit/issues/976
     */
        private val fAssumption: String?, private val fValueMatcher: Boolean = false, private val fValue: Any? = null, private val fMatcher: Matcher<*>? = null) : RuntimeException(), SelfDescribing {

    init {

        if (fValue is Throwable) {
            initCause(fValue as Throwable?)
        }
    }

    /**
     * An assumption exception with the given *value* (String or
     * Throwable) and an additional failing [Matcher].

     */
    @Deprecated("")
    @Deprecated("Please use {@link org.junit.AssumptionViolatedException} instead.")
    constructor(value: Any, matcher: Matcher<*>) : this(null, true, value, matcher) {
    }

    /**
     * An assumption exception with the given *value* (String or
     * Throwable) and an additional failing [Matcher].

     */
    @Deprecated("")
    @Deprecated("Please use {@link org.junit.AssumptionViolatedException} instead.")
    constructor(assumption: String, value: Any, matcher: Matcher<*>) : this(assumption, true, value, matcher) {
    }

    /**
     * An assumption exception with the given message and a cause.

     */
    @Deprecated("")
    @Deprecated("Please use {@link org.junit.AssumptionViolatedException} instead.")
    constructor(assumption: String, e: Throwable) : this(assumption, false, null, null) {
        initCause(e)
    }

    override fun getMessage(): String {
        return StringDescription.asString(this)
    }

    override fun describeTo(description: Description) {
        if (fAssumption != null) {
            description.appendText(fAssumption)
        }

        if (fValueMatcher) {
            // a value was passed in when this instance was constructed; print it
            if (fAssumption != null) {
                description.appendText(": ")
            }

            description.appendText("got: ")
            description.appendValue(fValue)

            if (fMatcher != null) {
                description.appendText(", expected: ")
                description.appendDescriptionOf(fMatcher)
            }
        }
    }

    companion object {
        private val serialVersionUID = 2L
    }
}
/**
 * An assumption exception with the given message only.

 */
