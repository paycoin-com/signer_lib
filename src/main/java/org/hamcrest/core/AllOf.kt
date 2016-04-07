package org.hamcrest.core

import org.hamcrest.Description
import org.hamcrest.DiagnosingMatcher
import org.hamcrest.Factory
import org.hamcrest.Matcher

import java.util.ArrayList
import java.util.Arrays

/**
 * Calculates the logical conjunction of multiple matchers. Evaluation is shortcut, so
 * subsequent matchers are not called if an earlier matcher returns `false`.
 */
class AllOf<T>(private val matchers: Iterable<Matcher<in T>>) : DiagnosingMatcher<T>() {

    public override fun matches(o: Any, mismatch: Description): Boolean {
        for (matcher in matchers) {
            if (!matcher.matches(o)) {
                mismatch.appendDescriptionOf(matcher).appendText(" ")
                matcher.describeMismatch(o, mismatch)
                return false
            }
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendList("(", " " + "and" + " ", ")", matchers)
    }

    companion object {

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(matchers: Iterable<Matcher<in T>>): Matcher<T> {
            return AllOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(vararg matchers: Matcher<in T>): Matcher<T> {
            return allOf(Arrays.asList(*matchers))
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(first: Matcher<in T>, second: Matcher<in T>): Matcher<T> {
            val matchers = ArrayList<Matcher<in T>>(2)
            matchers.add(first)
            matchers.add(second)
            return allOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(first: Matcher<in T>, second: Matcher<in T>, third: Matcher<in T>): Matcher<T> {
            val matchers = ArrayList<Matcher<in T>>(3)
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            return allOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(first: Matcher<in T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>): Matcher<T> {
            val matchers = ArrayList<Matcher<in T>>(4)
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            return allOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(first: Matcher<in T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>, fifth: Matcher<in T>): Matcher<T> {
            val matchers = ArrayList<Matcher<in T>>(5)
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            matchers.add(fifth)
            return allOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ALL** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", allOf(startsWith("my"), containsString("Val")))
         */
        @Factory
        fun <T> allOf(first: Matcher<in T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>, fifth: Matcher<in T>, sixth: Matcher<in T>): Matcher<T> {
            val matchers = ArrayList<Matcher<in T>>(6)
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            matchers.add(fifth)
            matchers.add(sixth)
            return allOf(matchers)
        }
    }
}
