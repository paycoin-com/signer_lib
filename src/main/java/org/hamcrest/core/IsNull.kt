/*  Copyright (c) 2000-2010 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.core.IsNot.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Factory
import org.hamcrest.BaseMatcher

/**
 * Is the value null?
 */
class IsNull<T> : BaseMatcher<T>() {
    override fun matches(o: Any?): Boolean {
        return o == null
    }

    override fun describeTo(description: Description) {
        description.appendText("null")
    }

    companion object {

        /**
         * Creates a matcher that matches if examined object is `null`.
         *
         *
         * For example:
         * assertThat(cheese, is(nullValue())

         */
        @Factory
        fun nullValue(): Matcher<Any> {
            return IsNull()
        }

        /**
         * A shortcut to the frequently used `not(nullValue())`.
         *
         *
         * For example:
         * assertThat(cheese, is(notNullValue()))
         * instead of:
         * assertThat(cheese, is(not(nullValue())))

         */
        @Factory
        fun notNullValue(): Matcher<Any> {
            return not(nullValue())
        }

        /**
         * Creates a matcher that matches if examined object is `null`. Accepts a
         * single dummy argument to facilitate type inference.
         *
         *
         * For example:
         * assertThat(cheese, is(nullValue(Cheese.class))

         * @param type
         * *     dummy parameter used to infer the generic type of the returned matcher
         */
        @Factory
        fun <T> nullValue(type: Class<T>): Matcher<T> {
            return IsNull()
        }

        /**
         * A shortcut to the frequently used `not(nullValue(X.class)). Accepts a
         * single dummy argument to facilitate type inference.`.
         *
         *
         * For example:
         * assertThat(cheese, is(notNullValue(X.class)))
         * instead of:
         * assertThat(cheese, is(not(nullValue(X.class))))

         * @param type
         * *     dummy parameter used to infer the generic type of the returned matcher
         */
        @Factory
        fun <T> notNullValue(type: Class<T>): Matcher<T> {
            return not(nullValue(type))
        }
    }
}

