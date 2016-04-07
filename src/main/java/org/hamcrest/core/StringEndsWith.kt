/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.Factory
import org.hamcrest.Matcher

/**
 * Tests if the argument is a string that contains a substring.
 */
class StringEndsWith(substring: String) : SubstringMatcher(substring) {

    override fun evalSubstringOf(s: String): Boolean {
        return s.endsWith(substring)
    }

    override fun relationship(): String {
        return "ending with"
    }

    companion object {

        /**
         * Creates a matcher that matches if the examined [String] ends with the specified
         * [String].
         *
         *
         * For example:
         * assertThat("myStringOfNote", endsWith("Note"))

         * @param suffix
         * *      the substring that the returned matcher will expect at the end of any examined string
         */
        @Factory
        fun endsWith(suffix: String): Matcher<String> {
            return StringEndsWith(suffix)
        }
    }

}
