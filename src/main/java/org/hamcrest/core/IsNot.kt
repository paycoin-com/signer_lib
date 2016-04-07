/*  Copyright (c) 2000-2009 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

import org.hamcrest.core.IsEqual.equalTo


/**
 * Calculates the logical negation of a matcher.
 */
class IsNot<T>(private val matcher: Matcher<T>) : BaseMatcher<T>() {

    override fun matches(arg: Any): Boolean {
        return !matcher.matches(arg)
    }

    override fun describeTo(description: Description) {
        description.appendText("not ").appendDescriptionOf(matcher)
    }

    companion object {


        /**
         * Creates a matcher that wraps an existing matcher, but inverts the logic by which
         * it will match.
         *
         *
         * For example:
         * assertThat(cheese, is(not(equalTo(smelly))))

         * @param matcher
         * *     the matcher whose sense should be inverted
         */
        @Factory
        fun <T> not(matcher: Matcher<T>): Matcher<T> {
            return IsNot(matcher)
        }

        /**
         * A shortcut to the frequently used `not(equalTo(x))`.
         *
         *
         * For example:
         * assertThat(cheese, is(not(smelly)))
         * instead of:
         * assertThat(cheese, is(not(equalTo(smelly))))

         * @param value
         * *     the value that any examined object should **not** equal
         */
        @Factory
        fun <T> not(value: T): Matcher<T> {
            return not(equalTo(value))
        }
    }
}
