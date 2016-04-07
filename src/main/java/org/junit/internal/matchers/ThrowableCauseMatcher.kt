package org.junit.internal.matchers

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * A matcher that applies a delegate matcher to the cause of the current Throwable, returning the result of that
 * match.

 * @param  the type of the throwable being matched
 */
class ThrowableCauseMatcher<T : Throwable>(private val causeMatcher: Matcher<out Throwable>) : TypeSafeMatcher<T>() {

    override fun describeTo(description: Description) {
        description.appendText("exception with cause ")
        description.appendDescriptionOf(causeMatcher)
    }

    override fun matchesSafely(item: T): Boolean {
        return causeMatcher.matches(item.cause)
    }

    override fun describeMismatchSafely(item: T, description: Description) {
        description.appendText("cause ")
        causeMatcher.describeMismatch(item.cause, description)
    }

    companion object {

        /**
         * Returns a matcher that verifies that the outer exception has a cause for which the supplied matcher
         * evaluates to true.

         * @param matcher to apply to the cause of the outer exception
         * *
         * @param  type of the outer exception
         */
        @Factory
        fun <T : Throwable> hasCause(matcher: Matcher<out Throwable>): Matcher<T> {
            return ThrowableCauseMatcher(matcher)
        }
    }
}