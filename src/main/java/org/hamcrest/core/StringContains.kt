/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.Factory
import org.hamcrest.Matcher

/**
 * Tests if the argument is a string that contains a substring.
 */
class StringContains(substring: String) : SubstringMatcher(substring) {

    override fun evalSubstringOf(s: String): Boolean {
        return s.indexOf(substring) >= 0
    }

    override fun relationship(): String {
        return "containing"
    }

    companion object {

        /**
         * Creates a matcher that matches if the examined [String] contains the specified
         * [String] anywhere.
         *
         *
         * For example:
         * assertThat("myStringOfNote", containsString("ring"))

         * @param substring
         * *     the substring that the returned matcher will expect to find within any examined string
         */
        @Factory
        fun containsString(substring: String): Matcher<String> {
            return StringContains(substring)
        }
    }

}