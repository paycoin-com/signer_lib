/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

import java.lang.reflect.Array


/**
 * Is the value equal to another value, as tested by the
 * [java.lang.Object.equals] invokedMethod?
 */
class IsEqual<T>(equalArg: T) : BaseMatcher<T>() {
    private val expectedValue: Any

    init {
        expectedValue = equalArg
    }

    override fun matches(actualValue: Any): Boolean {
        return areEqual(actualValue, expectedValue)
    }

    override fun describeTo(description: Description) {
        description.appendValue(expectedValue)
    }

    companion object {

        private fun areEqual(actual: Any?, expected: Any?): Boolean {
            if (actual == null) {
                return expected == null
            }

            if (expected != null && isArray(actual)) {
                return isArray(expected) && areArraysEqual(actual, expected)
            }

            return actual == expected
        }

        private fun areArraysEqual(actualArray: Any, expectedArray: Any): Boolean {
            return areArrayLengthsEqual(actualArray, expectedArray) && areArrayElementsEqual(actualArray, expectedArray)
        }

        private fun areArrayLengthsEqual(actualArray: Any, expectedArray: Any): Boolean {
            return Array.getLength(actualArray) == Array.getLength(expectedArray)
        }

        private fun areArrayElementsEqual(actualArray: Any, expectedArray: Any): Boolean {
            for (i in 0..Array.getLength(actualArray) - 1) {
                if (!areEqual(Array.get(actualArray, i), Array.get(expectedArray, i))) {
                    return false
                }
            }
            return true
        }

        private fun isArray(o: Any): Boolean {
            return o.javaClass.isArray
        }

        /**
         * Creates a matcher that matches when the examined object is logically equal to the specified
         * `operand`, as determined by calling the [java.lang.Object.equals] method on
         * the **examined** object.

         *
         * If the specified operand is `null` then the created matcher will only match if
         * the examined object's `equals` method returns `true` when passed a
         * `null` (which would be a violation of the `equals` contract), unless the
         * examined object itself is `null`, in which case the matcher will return a positive
         * match.

         *
         * The created matcher provides a special behaviour when examining `Array`s, whereby
         * it will match if both the operand and the examined object are arrays of the same length and
         * contain items that are equal to each other (according to the above rules) **in the same
         * indexes**.
         *
         *
         * For example:
         *
         * assertThat("foo", equalTo("foo"));
         * assertThat(new String[] {"foo", "bar"}, equalTo(new String[] {"foo", "bar"}));
         *

         */
        @Factory
        fun <T> equalTo(operand: T): Matcher<T> {
            return IsEqual(operand)
        }
    }
}
