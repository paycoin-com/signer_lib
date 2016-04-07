/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Factory
import org.hamcrest.BaseMatcher


/**
 * A matcher that always returns `true`.
 */
class IsAnything<T> @JvmOverloads constructor(private val message: String = "ANYTHING") : BaseMatcher<T>() {

    override fun matches(o: Any): Boolean {
        return true
    }

    override fun describeTo(description: Description) {
        description.appendText(message)
    }

    companion object {

        /**
         * Creates a matcher that always matches, regardless of the examined object.
         */
        @Factory
        fun anything(): Matcher<Any> {
            return IsAnything()
        }

        /**
         * Creates a matcher that always matches, regardless of the examined object, but describes
         * itself with the specified [String].

         * @param description
         * *     a meaningful [String] used when describing itself
         */
        @Factory
        fun anything(description: String): Matcher<Any> {
            return IsAnything(description)
        }
    }
}
