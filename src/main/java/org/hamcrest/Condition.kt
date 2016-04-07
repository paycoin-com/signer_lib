package org.hamcrest

/**
 * A Condition implements part of a multi-step match. We sometimes need to write matchers
 * that have a sequence of steps, where each step depends on the result of the previous
 * step and we can stop processing as soon as a step fails. These classes provide
 * infrastructure for writing such a sequence.

 * Based on https://github.com/npryce/maybe-java
 * @author Steve Freeman 2012 http://www.hamcrest.com
 */

abstract class Condition<T> private constructor() {

    interface Step<I, O> {
        fun apply(value: I, mismatch: Description): Condition<O>
    }

    abstract fun matching(match: Matcher<T>, message: String): Boolean
    abstract fun <U> and(mapping: Step<in T, U>): Condition<U>

    fun matching(match: Matcher<T>): Boolean {
        return matching(match, "")
    }

    fun <U> then(mapping: Step<in T, U>): Condition<U> {
        return and(mapping)
    }

    private class Matched<T> private constructor(private val theValue: T, private val mismatch: Description) : Condition<T>() {

        override fun matching(matcher: Matcher<T>, message: String): Boolean {
            if (matcher.matches(theValue)) {
                return true
            }
            mismatch.appendText(message)
            matcher.describeMismatch(theValue, mismatch)
            return false
        }

        override fun <U> and(next: Step<in T, U>): Condition<U> {
            return next.apply(theValue, mismatch)
        }
    }

    private class NotMatched<T> : Condition<T>() {
        override fun matching(match: Matcher<T>, message: String): Boolean {
            return false
        }

        override fun <U> and(mapping: Step<in T, U>): Condition<U> {
            return notMatched()
        }
    }

    companion object {
        val NOT_MATCHED = NotMatched<Any>()

        @SuppressWarnings("unchecked")
        fun <T> notMatched(): Condition<T> {
            return NOT_MATCHED as Condition<T>
        }

        fun <T> matched(theValue: T, mismatch: Description): Condition<T> {
            return Matched(theValue, mismatch)
        }
    }
}
