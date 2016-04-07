package org.junit.internal.matchers

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class ThrowableMessageMatcher<T : Throwable>(private val matcher: Matcher<String>) : TypeSafeMatcher<T>() {

    override fun describeTo(description: Description) {
        description.appendText("exception with message ")
        description.appendDescriptionOf(matcher)
    }

    override fun matchesSafely(item: T): Boolean {
        return matcher.matches(item.message)
    }

    override fun describeMismatchSafely(item: T, description: Description) {
        description.appendText("message ")
        matcher.describeMismatch(item.message, description)
    }

    companion object {

        @Factory
        fun <T : Throwable> hasMessage(matcher: Matcher<String>): Matcher<T> {
            return ThrowableMessageMatcher(matcher)
        }
    }
}