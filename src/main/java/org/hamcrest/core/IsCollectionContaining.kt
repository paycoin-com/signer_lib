package org.hamcrest.core

import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsEqual.equalTo

import java.util.ArrayList

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

class IsCollectionContaining<T>(private val elementMatcher: Matcher<in T>) : TypeSafeDiagnosingMatcher<Iterable<in T>>() {

    override fun matchesSafely(collection: Iterable<in T>, mismatchDescription: Description): Boolean {
        var isPastFirst = false
        for (item in collection) {
            if (elementMatcher.matches(item)) {
                return true
            }
            if (isPastFirst) {
                mismatchDescription.appendText(", ")
            }
            elementMatcher.describeMismatch(item, mismatchDescription)
            isPastFirst = true
        }
        return false
    }

    override fun describeTo(description: Description) {
        description.appendText("a collection containing ").appendDescriptionOf(elementMatcher)
    }

    companion object {


        /**
         * Creates a matcher for [Iterable]s that only matches when a single pass over the
         * examined [Iterable] yields at least one item that is matched by the specified
         * `itemMatcher`.  Whilst matching, the traversal of the examined [Iterable]
         * will stop as soon as a matching item is found.
         *
         *
         * For example:
         * assertThat(Arrays.asList("foo", "bar"), hasItem(startsWith("ba")))

         * @param itemMatcher
         * *     the matcher to apply to items provided by the examined [Iterable]
         */
        @Factory
        fun <T> hasItem(itemMatcher: Matcher<in T>): Matcher<Iterable<in T>> {
            return IsCollectionContaining(itemMatcher)
        }

        /**
         * Creates a matcher for [Iterable]s that only matches when a single pass over the
         * examined [Iterable] yields at least one item that is equal to the specified
         * `item`.  Whilst matching, the traversal of the examined [Iterable]
         * will stop as soon as a matching item is found.
         *
         *
         * For example:
         * assertThat(Arrays.asList("foo", "bar"), hasItem("bar"))

         * @param item
         * *     the item to compare against the items provided by the examined [Iterable]
         */
        @Factory
        fun <T> hasItem(item: T): Matcher<Iterable<in T>> {
            // Doesn't forward to hasItem() method so compiler can sort out generics.
            return IsCollectionContaining(equalTo(item))
        }

        /**
         * Creates a matcher for [Iterable]s that matches when consecutive passes over the
         * examined [Iterable] yield at least one item that is matched by the corresponding
         * matcher from the specified `itemMatchers`.  Whilst matching, each traversal of
         * the examined [Iterable] will stop as soon as a matching item is found.
         *
         *
         * For example:
         * assertThat(Arrays.asList("foo", "bar", "baz"), hasItems(endsWith("z"), endsWith("o")))

         * @param itemMatchers
         * *     the matchers to apply to items provided by the examined [Iterable]
         */
        @Factory
        fun <T> hasItems(vararg itemMatchers: Matcher<in T>): Matcher<Iterable<T>> {
            val all = ArrayList<Matcher<in Iterable<T>>>(itemMatchers.size)

            for (elementMatcher in itemMatchers) {
                // Doesn't forward to hasItem() method so compiler can sort out generics.
                all.add(IsCollectionContaining(elementMatcher))
            }

            return allOf(all)
        }

        /**
         * Creates a matcher for [Iterable]s that matches when consecutive passes over the
         * examined [Iterable] yield at least one item that is equal to the corresponding
         * item from the specified `items`.  Whilst matching, each traversal of the
         * examined [Iterable] will stop as soon as a matching item is found.
         *
         *
         * For example:
         * assertThat(Arrays.asList("foo", "bar", "baz"), hasItems("baz", "foo"))

         * @param items
         * *     the items to compare against the items provided by the examined [Iterable]
         */
        @Factory
        fun <T> hasItems(vararg items: T): Matcher<Iterable<T>> {
            val all = ArrayList<Matcher<in Iterable<T>>>(items.size)
            for (element in items) {
                all.add(hasItem(element))
            }

            return allOf(all)
        }
    }

}
