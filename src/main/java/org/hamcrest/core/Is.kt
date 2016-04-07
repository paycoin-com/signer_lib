package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsInstanceOf.instanceOf

/**
 * Decorates another Matcher, retaining the behaviour but allowing tests
 * to be slightly more expressive.

 * For example:  assertThat(cheese, equalTo(smelly))
 * vs.  assertThat(cheese, is(equalTo(smelly)))
 */
class Is<T>(private val matcher: Matcher<T>) : BaseMatcher<T>() {

    override fun matches(arg: Any): Boolean {
        return matcher.matches(arg)
    }

    override fun describeTo(description: Description) {
        description.appendText("is ").appendDescriptionOf(matcher)
    }

    override fun describeMismatch(item: Any, mismatchDescription: Description) {
        matcher.describeMismatch(item, mismatchDescription)
    }

    companion object {

        /**
         * Decorates another Matcher, retaining its behaviour, but allowing tests
         * to be slightly more expressive.
         *
         *
         * For example:
         * assertThat(cheese, is(equalTo(smelly)))
         * instead of:
         * assertThat(cheese, equalTo(smelly))

         */
        @Factory
        fun <T> `is`(matcher: Matcher<T>): Matcher<T> {
            return Is(matcher)
        }

        /**
         * A shortcut to the frequently used `is(equalTo(x))`.
         *
         *
         * For example:
         * assertThat(cheese, is(smelly))
         * instead of:
         * assertThat(cheese, is(equalTo(smelly)))

         */
        @Factory
        fun <T> `is`(value: T): Matcher<T> {
            return `is`(equalTo(value))
        }

        /**
         * A shortcut to the frequently used `is(instanceOf(SomeClass.class))`.
         *
         *
         * For example:
         * assertThat(cheese, is(Cheddar.class))
         * instead of:
         * assertThat(cheese, is(instanceOf(Cheddar.class)))

         */
        @Factory
        @Deprecated("")
        @Deprecated("use isA(Class<T> type) instead.")
        fun <T> `is`(type: Class<T>): Matcher<T> {
            val typeMatcher = instanceOf<T>(type)
            return `is`(typeMatcher)
        }

        /**
         * A shortcut to the frequently used `is(instanceOf(SomeClass.class))`.
         *
         *
         * For example:
         * assertThat(cheese, isA(Cheddar.class))
         * instead of:
         * assertThat(cheese, is(instanceOf(Cheddar.class)))

         */
        @Factory
        fun <T> isA(type: Class<T>): Matcher<T> {
            val typeMatcher = instanceOf<T>(type)
            return `is`(typeMatcher)
        }
    }
}
