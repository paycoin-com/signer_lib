package org.hamcrest.core

import java.util.ArrayList
import java.util.Arrays

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

/**
 * Calculates the logical disjunction of multiple matchers. Evaluation is shortcut, so
 * subsequent matchers are not called if an earlier matcher returns `true`.
 */
class AnyOf<T>(matchers: Iterable<Matcher<in T>>) : ShortcutCombination<T>(matchers) {

    override fun matches(o: Any): Boolean {
        return matches(o, true)
    }

    override fun describeTo(description: Description) {
        describeTo(description, "or")
    }

    companion object {

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(matchers: Iterable<Matcher<in T>>): AnyOf<T> {
            return AnyOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(vararg matchers: Matcher<in T>): AnyOf<T> {
            return anyOf(Arrays.asList(*matchers))
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(first: Matcher<T>, second: Matcher<in T>): AnyOf<T> {
            val matchers = ArrayList<Matcher<in T>>()
            matchers.add(first)
            matchers.add(second)
            return anyOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(first: Matcher<T>, second: Matcher<in T>, third: Matcher<in T>): AnyOf<T> {
            val matchers = ArrayList<Matcher<in T>>()
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            return anyOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(first: Matcher<T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>): AnyOf<T> {
            val matchers = ArrayList<Matcher<in T>>()
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            return anyOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(first: Matcher<T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>, fifth: Matcher<in T>): AnyOf<T> {
            val matchers = ArrayList<Matcher<in T>>()
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            matchers.add(fifth)
            return anyOf(matchers)
        }

        /**
         * Creates a matcher that matches if the examined object matches **ANY** of the specified matchers.
         *
         *
         * For example:
         * assertThat("myValue", anyOf(startsWith("foo"), containsString("Val")))
         */
        @Factory
        fun <T> anyOf(first: Matcher<T>, second: Matcher<in T>, third: Matcher<in T>, fourth: Matcher<in T>, fifth: Matcher<in T>, sixth: Matcher<in T>): AnyOf<T> {
            val matchers = ArrayList<Matcher<in T>>()
            matchers.add(first)
            matchers.add(second)
            matchers.add(third)
            matchers.add(fourth)
            matchers.add(fifth)
            matchers.add(sixth)
            return anyOf(matchers)
        }
    }
}
