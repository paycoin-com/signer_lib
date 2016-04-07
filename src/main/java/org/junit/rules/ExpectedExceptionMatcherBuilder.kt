package org.junit.rules

import org.hamcrest.CoreMatchers.allOf
import org.junit.matchers.JUnitMatchers.isThrowable

import java.util.ArrayList

import org.hamcrest.Matcher

/**
 * Builds special matcher used by [ExpectedException].
 */
internal class ExpectedExceptionMatcherBuilder {

    private val matchers = ArrayList<Matcher<*>>()

    fun add(matcher: Matcher<*>) {
        matchers.add(matcher)
    }

    fun expectsThrowable(): Boolean {
        return !matchers.isEmpty()
    }

    fun build(): Matcher<Throwable> {
        return isThrowable(allOfTheMatchers())
    }

    private fun allOfTheMatchers(): Matcher<Throwable> {
        if (matchers.size == 1) {
            return cast(matchers[0])
        }
        return allOf(castedMatchers())
    }

    @SuppressWarnings("unchecked", "rawtypes")
    private fun castedMatchers(): List<Matcher<in Throwable>> {
        return ArrayList<Matcher<in Throwable>>(matchers as List<Any>)
    }

    @SuppressWarnings("unchecked")
    private fun cast(singleMatcher: Matcher<*>): Matcher<Throwable> {
        return singleMatcher as Matcher<Throwable>
    }
}
