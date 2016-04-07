package org.hamcrest.core

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

abstract class SubstringMatcher protected constructor(// TODO: Replace String with CharSequence to allow for easy interopability between
        //       String, StringBuffer, StringBuilder, CharBuffer, etc (joe).

        protected val substring: String) : TypeSafeMatcher<String>() {

    public override fun matchesSafely(item: String): Boolean {
        return evalSubstringOf(item)
    }

    public override fun describeMismatchSafely(item: String, mismatchDescription: Description) {
        mismatchDescription.appendText("was \"").appendText(item).appendText("\"")
    }

    override fun describeTo(description: Description) {
        description.appendText("a string ").appendText(relationship()).appendText(" ").appendValue(substring)
    }

    protected abstract fun evalSubstringOf(string: String): Boolean

    protected abstract fun relationship(): String
}