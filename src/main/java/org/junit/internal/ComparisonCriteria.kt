package org.junit.internal

import java.lang.reflect.Array
import java.util.Arrays

import org.junit.Assert

/**
 * Defines criteria for finding two items "equal enough". Concrete subclasses
 * may demand exact equality, or, for example, equality within a given delta.
 */
abstract class ComparisonCriteria {
    /**
     * Asserts that two arrays are equal, according to the criteria defined by
     * the concrete subclass. If they are not, an [AssertionError] is
     * thrown with the given message. If `expecteds` and
     * `actuals` are `null`, they are considered equal.

     * @param message the identifying message for the [AssertionError] (
     * * `null` okay)
     * *
     * @param expecteds Object array or array of arrays (multi-dimensional array) with
     * * expected values.
     * *
     * @param actuals Object array or array of arrays (multi-dimensional array) with
     * * actual values
     */
    @Throws(ArrayComparisonFailure::class)
    fun arrayEquals(message: String?, expecteds: Any, actuals: Any) {
        if (expecteds === actuals || Arrays.deepEquals(arrayOf(expecteds), arrayOf(actuals))) {
            // The reflection-based loop below is potentially very slow, especially for primitive
            // arrays. The deepEquals check allows us to circumvent it in the usual case where
            // the arrays are exactly equal.
            return
        }
        val header = if (message == null) "" else message + ": "

        val expectedsLength = assertArraysAreSameLength(expecteds,
                actuals, header)

        for (i in 0..expectedsLength - 1) {
            val expected = Array.get(expecteds, i)
            val actual = Array.get(actuals, i)

            if (isArray(expected) && isArray(actual)) {
                try {
                    arrayEquals(message, expected, actual)
                } catch (e: ArrayComparisonFailure) {
                    e.addDimension(i)
                    throw e
                }

            } else {
                try {
                    assertElementsEqual(expected, actual)
                } catch (e: AssertionError) {
                    throw ArrayComparisonFailure(header, e, i)
                }

            }
        }
    }

    private fun isArray(expected: Any?): Boolean {
        return expected != null && expected.javaClass.isArray
    }

    private fun assertArraysAreSameLength(expecteds: Any?,
                                          actuals: Any?, header: String): Int {
        if (expecteds == null) {
            Assert.fail(header + "expected array was null")
        }
        if (actuals == null) {
            Assert.fail(header + "actual array was null")
        }
        val actualsLength = Array.getLength(actuals)
        val expectedsLength = Array.getLength(expecteds)
        if (actualsLength != expectedsLength) {
            Assert.fail(header + "array lengths differed, expected.length="
                    + expectedsLength + " actual.length=" + actualsLength)
        }
        return expectedsLength
    }

    protected abstract fun assertElementsEqual(expected: Any, actual: Any)
}
