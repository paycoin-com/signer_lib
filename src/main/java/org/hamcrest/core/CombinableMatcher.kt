package org.hamcrest.core

import org.hamcrest.*

import java.util.ArrayList

class CombinableMatcher<T>(private val matcher: Matcher<in T>) : TypeSafeDiagnosingMatcher<T>() {

    override fun matchesSafely(item: T, mismatch: Description): Boolean {
        if (!matcher.matches(item)) {
            matcher.describeMismatch(item, mismatch)
            return false
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendDescriptionOf(matcher)
    }

    fun and(other: Matcher<in T>): CombinableMatcher<T> {
        return CombinableMatcher(AllOf(templatedListWith(other)))
    }

    fun or(other: Matcher<in T>): CombinableMatcher<T> {
        return CombinableMatcher(AnyOf(templatedListWith(other)))
    }

    private fun templatedListWith(other: Matcher<in T>): ArrayList<Matcher<in T>> {
        val matchers = ArrayList<Matcher<in T>>()
        matchers.add(matcher)
        matchers.add(other)
        return matchers
    }

    class CombinableBothMatcher<X>(private val first: Matcher<in X>) {
        fun and(other: Matcher<in X>): CombinableMatcher<X> {
            return CombinableMatcher(first).and(other)
        }
    }

    class CombinableEitherMatcher<X>(private val first: Matcher<in X>) {
        fun or(other: Matcher<in X>): CombinableMatcher<X> {
            return CombinableMatcher(first).or(other)
        }
    }

    companion object {

        /**
         * Creates a matcher that matches when both of the specified matchers match the examined object.
         *
         *
         * For example:
         * assertThat("fab", both(containsString("a")).and(containsString("b")))
         */
        @Factory
        fun <LHS> both(matcher: Matcher<in LHS>): CombinableBothMatcher<LHS> {
            return CombinableBothMatcher(matcher)
        }

        /**
         * Creates a matcher that matches when either of the specified matchers match the examined object.
         *
         *
         * For example:
         * assertThat("fan", either(containsString("a")).and(containsString("b")))
         */
        @Factory
        fun <LHS> either(matcher: Matcher<in LHS>): CombinableEitherMatcher<LHS> {
            return CombinableEitherMatcher(matcher)
        }
    }
}