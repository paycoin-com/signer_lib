package org.hamcrest.core

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

class Every<T>(private val matcher: Matcher<in T>) : TypeSafeDiagnosingMatcher<Iterable<T>>() {

    public override fun matchesSafely(collection: Iterable<T>, mismatchDescription: Description): Boolean {
        for (t in collection) {
            if (!matcher.matches(t)) {
                mismatchDescription.appendText("an item ")
                matcher.describeMismatch(t, mismatchDescription)
                return false
            }
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendText("every item is ").appendDescriptionOf(matcher)
    }

    companion object {

        /**
         * Creates a matcher for [Iterable]s that only matches when a single pass over the
         * examined [Iterable] yields items that are all matched by the specified
         * `itemMatcher`.
         *
         *
         * For example:
         * assertThat(Arrays.asList("bar", "baz"), everyItem(startsWith("ba")))

         * @param itemMatcher
         * *     the matcher to apply to every item provided by the examined [Iterable]
         */
        @Factory
        fun <U> everyItem(itemMatcher: Matcher<U>): Matcher<Iterable<U>> {
            return Every(itemMatcher)
        }
    }
}
