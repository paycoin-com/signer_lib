/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.Factory
import org.hamcrest.Matcher

/**
 * Tests if the argument is a string that contains a substring.
 */
class StringStartsWith(substring: String) : SubstringMatcher(substring) {

    override fun evalSubstringOf(s: String): Boolean {
        return s.startsWith(substring)
    }

    override fun relationship(): String {
        return "starting with"
    }

    companion object {

        /**
         * Creates a matcher that matches if the examined [String] starts with the specified
         * [String].
         *
         *
         * For example:
         * assertThat("myStringOfNote", startsWith("my"))

         * @param prefix
         * *      the substring that the returned matcher will expect at the start of any examined string
         */
        @Factory
        fun startsWith(prefix: String): Matcher<String> {
            return StringStartsWith(prefix)
        }
    }

}