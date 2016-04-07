package org.junit

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.junit.internal.ArrayComparisonFailure
import org.junit.internal.ExactComparisonCriteria
import org.junit.internal.InexactComparisonCriteria

/**
 * A set of assertion methods useful for writing tests. Only failed assertions
 * are recorded. These methods can be used directly:
 * `Assert.assertEquals(...)`, however, they read better if they
 * are referenced through static import:

 *
 * import static org.junit.Assert.*;
 * ...
 * assertEquals(...);
 *

 * @see AssertionError

 * @since 4.0
 */
class Assert
/**
 * Protect constructor since it is a static only class
 */
protected constructor() {
    companion object {

        /**
         * Asserts that a condition is true. If it isn't it throws an
         * [AssertionError] with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param condition condition to be checked
         */
        fun assertTrue(message: String?, condition: Boolean) {
            if (!condition) {
                fail(message)
            }
        }

        /**
         * Asserts that a condition is true. If it isn't it throws an
         * [AssertionError] without a message.

         * @param condition condition to be checked
         */
        fun assertTrue(condition: Boolean) {
            assertTrue(null, condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws an
         * [AssertionError] with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param condition condition to be checked
         */
        fun assertFalse(message: String?, condition: Boolean) {
            assertTrue(message, !condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws an
         * [AssertionError] without a message.

         * @param condition condition to be checked
         */
        fun assertFalse(condition: Boolean) {
            assertFalse(null, condition)
        }

        /**
         * Fails a test with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @see AssertionError
         */
        @JvmOverloads fun fail(message: String? = null) {
            if (message == null) {
                throw AssertionError()
            }
            throw AssertionError(message)
        }

        /**
         * Asserts that two objects are equal. If they are not, an
         * [AssertionError] is thrown with the given message. If
         * `expected` and `actual` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expected expected value
         * *
         * @param actual actual value
         */
        fun assertEquals(message: String?, expected: Any,
                         actual: Any) {
            if (equalsRegardingNull(expected, actual)) {
                return
            } else if (expected is String && actual is String) {
                val cleanMessage = message ?: ""
                throw ComparisonFailure(cleanMessage, expected,
                        actual)
            } else {
                failNotEquals(message, expected, actual)
            }
        }

        private fun equalsRegardingNull(expected: Any?, actual: Any?): Boolean {
            if (expected == null) {
                return actual == null
            }

            return isEquals(expected, actual)
        }

        private fun isEquals(expected: Any, actual: Any): Boolean {
            return expected == actual
        }

        /**
         * Asserts that two objects are equal. If they are not, an
         * [AssertionError] without a message is thrown. If
         * `expected` and `actual` are `null`,
         * they are considered equal.

         * @param expected expected value
         * *
         * @param actual the value to check against `expected`
         */
        fun assertEquals(expected: Any, actual: Any) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two objects are **not** equals. If they are, an
         * [AssertionError] is thrown with the given message. If
         * `unexpected` and `actual` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param unexpected unexpected value to check
         * *
         * @param actual the value to check against `unexpected`
         */
        fun assertNotEquals(message: String?, unexpected: Any,
                            actual: Any) {
            if (equalsRegardingNull(unexpected, actual)) {
                failEquals(message, actual)
            }
        }

        /**
         * Asserts that two objects are **not** equals. If they are, an
         * [AssertionError] without a message is thrown. If
         * `unexpected` and `actual` are `null`,
         * they are considered equal.

         * @param unexpected unexpected value to check
         * *
         * @param actual the value to check against `unexpected`
         */
        fun assertNotEquals(unexpected: Any, actual: Any) {
            assertNotEquals(null, unexpected, actual)
        }

        private fun failEquals(message: String?, actual: Any) {
            var formatted = "Values should be different. "
            if (message != null) {
                formatted = message + ". "
            }

            formatted += "Actual: " + actual
            fail(formatted)
        }

        /**
         * Asserts that two longs are **not** equals. If they are, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param unexpected unexpected value to check
         * *
         * @param actual the value to check against `unexpected`
         */
        fun assertNotEquals(message: String?, unexpected: Long, actual: Long) {
            if (unexpected == actual) {
                failEquals(message, java.lang.Long.valueOf(actual))
            }
        }

        /**
         * Asserts that two longs are **not** equals. If they are, an
         * [AssertionError] without a message is thrown.

         * @param unexpected unexpected value to check
         * *
         * @param actual the value to check against `unexpected`
         */
        fun assertNotEquals(unexpected: Long, actual: Long) {
            assertNotEquals(null, unexpected, actual)
        }

        /**
         * Asserts that two doubles are **not** equal to within a positive delta.
         * If they are, an [AssertionError] is thrown with the given
         * message. If the unexpected value is infinity then the delta value is
         * ignored. NaNs are considered equal:
         * `assertNotEquals(Double.NaN, Double.NaN, *)` fails

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param unexpected unexpected value
         * *
         * @param actual the value to check against `unexpected`
         * *
         * @param delta the maximum delta between `unexpected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertNotEquals(message: String?, unexpected: Double,
                            actual: Double, delta: Double) {
            if (!doubleIsDifferent(unexpected, actual, delta)) {
                failEquals(message, java.lang.Double.valueOf(actual))
            }
        }

        /**
         * Asserts that two doubles are **not** equal to within a positive delta.
         * If they are, an [AssertionError] is thrown. If the unexpected
         * value is infinity then the delta value is ignored.NaNs are considered
         * equal: `assertNotEquals(Double.NaN, Double.NaN, *)` fails

         * @param unexpected unexpected value
         * *
         * @param actual the value to check against `unexpected`
         * *
         * @param delta the maximum delta between `unexpected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertNotEquals(unexpected: Double, actual: Double, delta: Double) {
            assertNotEquals(null, unexpected, actual, delta)
        }

        /**
         * Asserts that two floats are **not** equal to within a positive delta.
         * If they are, an [AssertionError] is thrown. If the unexpected
         * value is infinity then the delta value is ignored.NaNs are considered
         * equal: `assertNotEquals(Float.NaN, Float.NaN, *)` fails

         * @param unexpected unexpected value
         * *
         * @param actual the value to check against `unexpected`
         * *
         * @param delta the maximum delta between `unexpected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertNotEquals(unexpected: Float, actual: Float, delta: Float) {
            assertNotEquals(null, unexpected, actual, delta)
        }

        /**
         * Asserts that two object arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message. If
         * `expecteds` and `actuals` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds Object array or array of arrays (multi-dimensional array) with
         * * expected values.
         * *
         * @param actuals Object array or array of arrays (multi-dimensional array) with
         * * actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: Array<Any>,
                              actuals: Array<Any>) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two object arrays are equal. If they are not, an
         * [AssertionError] is thrown. If `expected` and
         * `actual` are `null`, they are considered
         * equal.

         * @param expecteds Object array or array of arrays (multi-dimensional array) with
         * * expected values
         * *
         * @param actuals Object array or array of arrays (multi-dimensional array) with
         * * actual values
         */
        fun assertArrayEquals(expecteds: Array<Any>, actuals: Array<Any>) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two boolean arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message. If
         * `expecteds` and `actuals` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds boolean array with expected values.
         * *
         * @param actuals boolean array with expected values.
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: BooleanArray,
                              actuals: BooleanArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two boolean arrays are equal. If they are not, an
         * [AssertionError] is thrown. If `expected` and
         * `actual` are `null`, they are considered
         * equal.

         * @param expecteds boolean array with expected values.
         * *
         * @param actuals boolean array with expected values.
         */
        fun assertArrayEquals(expecteds: BooleanArray, actuals: BooleanArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two byte arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds byte array with expected values.
         * *
         * @param actuals byte array with actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: ByteArray,
                              actuals: ByteArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two byte arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds byte array with expected values.
         * *
         * @param actuals byte array with actual values
         */
        fun assertArrayEquals(expecteds: ByteArray, actuals: ByteArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two char arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds char array with expected values.
         * *
         * @param actuals char array with actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: CharArray,
                              actuals: CharArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two char arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds char array with expected values.
         * *
         * @param actuals char array with actual values
         */
        fun assertArrayEquals(expecteds: CharArray, actuals: CharArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two short arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds short array with expected values.
         * *
         * @param actuals short array with actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: ShortArray,
                              actuals: ShortArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two short arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds short array with expected values.
         * *
         * @param actuals short array with actual values
         */
        fun assertArrayEquals(expecteds: ShortArray, actuals: ShortArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two int arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds int array with expected values.
         * *
         * @param actuals int array with actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: IntArray,
                              actuals: IntArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two int arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds int array with expected values.
         * *
         * @param actuals int array with actual values
         */
        fun assertArrayEquals(expecteds: IntArray, actuals: IntArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two long arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds long array with expected values.
         * *
         * @param actuals long array with actual values
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: LongArray,
                              actuals: LongArray) {
            internalArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two long arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds long array with expected values.
         * *
         * @param actuals long array with actual values
         */
        fun assertArrayEquals(expecteds: LongArray, actuals: LongArray) {
            assertArrayEquals(null, expecteds, actuals)
        }

        /**
         * Asserts that two double arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds double array with expected values.
         * *
         * @param actuals double array with actual values
         * *
         * @param delta the maximum delta between `expecteds[i]` and
         * * `actuals[i]` for which both numbers are still
         * * considered equal.
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: DoubleArray,
                              actuals: DoubleArray, delta: Double) {
            InexactComparisonCriteria(delta).arrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two double arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds double array with expected values.
         * *
         * @param actuals double array with actual values
         * *
         * @param delta the maximum delta between `expecteds[i]` and
         * * `actuals[i]` for which both numbers are still
         * * considered equal.
         */
        fun assertArrayEquals(expecteds: DoubleArray, actuals: DoubleArray, delta: Double) {
            assertArrayEquals(null, expecteds, actuals, delta)
        }

        /**
         * Asserts that two float arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds float array with expected values.
         * *
         * @param actuals float array with actual values
         * *
         * @param delta the maximum delta between `expecteds[i]` and
         * * `actuals[i]` for which both numbers are still
         * * considered equal.
         */
        @Throws(ArrayComparisonFailure::class)
        fun assertArrayEquals(message: String?, expecteds: FloatArray,
                              actuals: FloatArray, delta: Float) {
            InexactComparisonCriteria(delta).arrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two float arrays are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expecteds float array with expected values.
         * *
         * @param actuals float array with actual values
         * *
         * @param delta the maximum delta between `expecteds[i]` and
         * * `actuals[i]` for which both numbers are still
         * * considered equal.
         */
        fun assertArrayEquals(expecteds: FloatArray, actuals: FloatArray, delta: Float) {
            assertArrayEquals(null, expecteds, actuals, delta)
        }

        /**
         * Asserts that two object arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message. If
         * `expecteds` and `actuals` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds Object array or array of arrays (multi-dimensional array) with
         * * expected values.
         * *
         * @param actuals Object array or array of arrays (multi-dimensional array) with
         * * actual values
         */
        @Throws(ArrayComparisonFailure::class)
        private fun internalArrayEquals(message: String, expecteds: Any,
                                        actuals: Any) {
            ExactComparisonCriteria().arrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two doubles are equal to within a positive delta.
         * If they are not, an [AssertionError] is thrown with the given
         * message. If the expected value is infinity then the delta value is
         * ignored. NaNs are considered equal:
         * `assertEquals(Double.NaN, Double.NaN, *)` passes

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expected expected value
         * *
         * @param actual the value to check against `expected`
         * *
         * @param delta the maximum delta between `expected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertEquals(message: String?, expected: Double,
                         actual: Double, delta: Double) {
            if (doubleIsDifferent(expected, actual, delta)) {
                failNotEquals(message, java.lang.Double.valueOf(expected), java.lang.Double.valueOf(actual))
            }
        }

        /**
         * Asserts that two floats are equal to within a positive delta.
         * If they are not, an [AssertionError] is thrown with the given
         * message. If the expected value is infinity then the delta value is
         * ignored. NaNs are considered equal:
         * `assertEquals(Float.NaN, Float.NaN, *)` passes

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expected expected value
         * *
         * @param actual the value to check against `expected`
         * *
         * @param delta the maximum delta between `expected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertEquals(message: String?, expected: Float,
                         actual: Float, delta: Float) {
            if (floatIsDifferent(expected, actual, delta)) {
                failNotEquals(message, java.lang.Float.valueOf(expected), java.lang.Float.valueOf(actual))
            }
        }

        /**
         * Asserts that two floats are **not** equal to within a positive delta.
         * If they are, an [AssertionError] is thrown with the given
         * message. If the unexpected value is infinity then the delta value is
         * ignored. NaNs are considered equal:
         * `assertNotEquals(Float.NaN, Float.NaN, *)` fails

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param unexpected unexpected value
         * *
         * @param actual the value to check against `unexpected`
         * *
         * @param delta the maximum delta between `unexpected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertNotEquals(message: String?, unexpected: Float,
                            actual: Float, delta: Float) {
            if (!floatIsDifferent(unexpected, actual, delta)) {
                failEquals(message, java.lang.Float.valueOf(actual))
            }
        }

        private fun doubleIsDifferent(d1: Double, d2: Double, delta: Double): Boolean {
            if (java.lang.Double.compare(d1, d2) == 0) {
                return false
            }
            if (Math.abs(d1 - d2) <= delta) {
                return false
            }

            return true
        }

        private fun floatIsDifferent(f1: Float, f2: Float, delta: Float): Boolean {
            if (java.lang.Float.compare(f1, f2) == 0) {
                return false
            }
            if (Math.abs(f1 - f2) <= delta) {
                return false
            }

            return true
        }

        /**
         * Asserts that two longs are equal. If they are not, an
         * [AssertionError] is thrown.

         * @param expected expected long value.
         * *
         * @param actual actual long value
         */
        fun assertEquals(expected: Long, actual: Long) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two longs are equal. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expected long expected value.
         * *
         * @param actual long actual value
         */
        fun assertEquals(message: String?, expected: Long, actual: Long) {
            if (expected != actual) {
                failNotEquals(message, java.lang.Long.valueOf(expected), java.lang.Long.valueOf(actual))
            }
        }


        @Deprecated("")
        @Deprecated("Use\n                  <code>assertEquals(double expected, double actual, double delta)</code>\n                  instead")
        fun assertEquals(expected: Double, actual: Double) {
            assertEquals(null, expected, actual)
        }


        @Deprecated("")
        @Deprecated("Use\n                  <code>assertEquals(String message, double expected, double actual, double delta)</code>\n                  instead")
        fun assertEquals(message: String?, expected: Double,
                         actual: Double) {
            fail("Use assertEquals(expected, actual, delta) to compare floating-point numbers")
        }

        /**
         * Asserts that two doubles are equal to within a positive delta.
         * If they are not, an [AssertionError] is thrown. If the expected
         * value is infinity then the delta value is ignored.NaNs are considered
         * equal: `assertEquals(Double.NaN, Double.NaN, *)` passes

         * @param expected expected value
         * *
         * @param actual the value to check against `expected`
         * *
         * @param delta the maximum delta between `expected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */
        fun assertEquals(expected: Double, actual: Double, delta: Double) {
            assertEquals(null, expected, actual, delta)
        }

        /**
         * Asserts that two floats are equal to within a positive delta.
         * If they are not, an [AssertionError] is thrown. If the expected
         * value is infinity then the delta value is ignored. NaNs are considered
         * equal: `assertEquals(Float.NaN, Float.NaN, *)` passes

         * @param expected expected value
         * *
         * @param actual the value to check against `expected`
         * *
         * @param delta the maximum delta between `expected` and
         * * `actual` for which both numbers are still
         * * considered equal.
         */

        fun assertEquals(expected: Float, actual: Float, delta: Float) {
            assertEquals(null, expected, actual, delta)
        }

        /**
         * Asserts that an object isn't null. If it is an [AssertionError] is
         * thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param object Object to check or `null`
         */
        fun assertNotNull(message: String?, `object`: Any?) {
            assertTrue(message, `object` != null)
        }

        /**
         * Asserts that an object isn't null. If it is an [AssertionError] is
         * thrown.

         * @param object Object to check or `null`
         */
        fun assertNotNull(`object`: Any) {
            assertNotNull(null, `object`)
        }

        /**
         * Asserts that an object is null. If it is not, an [AssertionError]
         * is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param object Object to check or `null`
         */
        fun assertNull(message: String?, `object`: Any?) {
            if (`object` == null) {
                return
            }
            failNotNull(message, `object`)
        }

        /**
         * Asserts that an object is null. If it isn't an [AssertionError] is
         * thrown.

         * @param object Object to check or `null`
         */
        fun assertNull(`object`: Any) {
            assertNull(null, `object`)
        }

        private fun failNotNull(message: String?, actual: Any) {
            var formatted = ""
            if (message != null) {
                formatted = message + " "
            }
            fail(formatted + "expected null, but was:<" + actual + ">")
        }

        /**
         * Asserts that two objects refer to the same object. If they are not, an
         * [AssertionError] is thrown with the given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expected the expected object
         * *
         * @param actual the object to compare to `expected`
         */
        fun assertSame(message: String?, expected: Any, actual: Any) {
            if (expected === actual) {
                return
            }
            failNotSame(message, expected, actual)
        }

        /**
         * Asserts that two objects refer to the same object. If they are not the
         * same, an [AssertionError] without a message is thrown.

         * @param expected the expected object
         * *
         * @param actual the object to compare to `expected`
         */
        fun assertSame(expected: Any, actual: Any) {
            assertSame(null, expected, actual)
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object, an [AssertionError] is thrown with the
         * given message.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param unexpected the object you don't expect
         * *
         * @param actual the object to compare to `unexpected`
         */
        fun assertNotSame(message: String?, unexpected: Any,
                          actual: Any) {
            if (unexpected === actual) {
                failSame(message)
            }
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object, an [AssertionError] without a message is
         * thrown.

         * @param unexpected the object you don't expect
         * *
         * @param actual the object to compare to `unexpected`
         */
        fun assertNotSame(unexpected: Any, actual: Any) {
            assertNotSame(null, unexpected, actual)
        }

        private fun failSame(message: String?) {
            var formatted = ""
            if (message != null) {
                formatted = message + " "
            }
            fail(formatted + "expected not same")
        }

        private fun failNotSame(message: String?, expected: Any,
                                actual: Any) {
            var formatted = ""
            if (message != null) {
                formatted = message + " "
            }
            fail(formatted + "expected same:<" + expected + "> was not:<" + actual
                    + ">")
        }

        private fun failNotEquals(message: String, expected: Any,
                                  actual: Any) {
            fail(format(message, expected, actual))
        }

        internal fun format(message: String?, expected: Any, actual: Any): String {
            var formatted = ""
            if (message != null && message != "") {
                formatted = message + " "
            }
            val expectedString = expected.toString()
            val actualString = actual.toString()
            if (expectedString == actualString) {
                return formatted + "expected: "
                +formatClassAndValue(expected, expectedString)
                +" but was: " + formatClassAndValue(actual, actualString)
            } else {
                return formatted + "expected:<" + expectedString + "> but was:<"
                +actualString + ">"
            }
        }

        private fun formatClassAndValue(value: Any?, valueString: String): String {
            val className = if (value == null) "null" else value.javaClass.name
            return "$className<$valueString>"
        }

        /**
         * Asserts that two object arrays are equal. If they are not, an
         * [AssertionError] is thrown with the given message. If
         * `expecteds` and `actuals` are `null`,
         * they are considered equal.

         * @param message the identifying message for the [AssertionError] (`null`
         * * okay)
         * *
         * @param expecteds Object array or array of arrays (multi-dimensional array) with
         * * expected values.
         * *
         * @param actuals Object array or array of arrays (multi-dimensional array) with
         * * actual values
         * *
         */
        @Deprecated("")
        @Deprecated("use assertArrayEquals")
        fun assertEquals(message: String, expecteds: Array<Any>,
                         actuals: Array<Any>) {
            assertArrayEquals(message, expecteds, actuals)
        }

        /**
         * Asserts that two object arrays are equal. If they are not, an
         * [AssertionError] is thrown. If `expected` and
         * `actual` are `null`, they are considered
         * equal.

         * @param expecteds Object array or array of arrays (multi-dimensional array) with
         * * expected values
         * *
         * @param actuals Object array or array of arrays (multi-dimensional array) with
         * * actual values
         * *
         */
        @Deprecated("")
        @Deprecated("use assertArrayEquals")
        fun assertEquals(expecteds: Array<Any>, actuals: Array<Any>) {
            assertArrayEquals(expecteds, actuals)
        }

        /**
         * Asserts that `actual` satisfies the condition specified by
         * `matcher`. If not, an [AssertionError] is thrown with
         * information about the matcher and failing value. Example:

         *
         * assertThat(0, is(1)); // fails:
         * // failure message:
         * // expected: is &lt;1&gt;
         * // got value: &lt;0&gt;
         * assertThat(0, is(not(1))) // passes
         *

         * `org.hamcrest.Matcher` does not currently document the meaning
         * of its type parameter `T`.  This method assumes that a matcher
         * typed as `Matcher&lt;T&gt;` can be meaningfully applied only
         * to values that could be assigned to a variable of type `T`.

         * @param  the static type accepted by the matcher (this can flag obvious
         * * compile-time problems such as `assertThat(1, is(&quot;a&quot;))`
         * *
         * @param actual the computed value being compared
         * *
         * @param matcher an expression, built of [Matcher]s, specifying allowed
         * * values
         * *
         * @see org.hamcrest.CoreMatchers

         * @see org.hamcrest.MatcherAssert
         */
        fun <T> assertThat(actual: T, matcher: Matcher<in T>) {
            assertThat("", actual, matcher)
        }

        /**
         * Asserts that `actual` satisfies the condition specified by
         * `matcher`. If not, an [AssertionError] is thrown with
         * the reason and information about the matcher and failing value. Example:

         *
         * assertThat(&quot;Help! Integers don't work&quot;, 0, is(1)); // fails:
         * // failure message:
         * // Help! Integers don't work
         * // expected: is &lt;1&gt;
         * // got value: &lt;0&gt;
         * assertThat(&quot;Zero is one&quot;, 0, is(not(1))) // passes
         *

         * `org.hamcrest.Matcher` does not currently document the meaning
         * of its type parameter `T`.  This method assumes that a matcher
         * typed as `Matcher&lt;T&gt;` can be meaningfully applied only
         * to values that could be assigned to a variable of type `T`.

         * @param reason additional information about the error
         * *
         * @param  the static type accepted by the matcher (this can flag obvious
         * * compile-time problems such as `assertThat(1, is(&quot;a&quot;))`
         * *
         * @param actual the computed value being compared
         * *
         * @param matcher an expression, built of [Matcher]s, specifying allowed
         * * values
         * *
         * @see org.hamcrest.CoreMatchers

         * @see org.hamcrest.MatcherAssert
         */
        fun <T> assertThat(reason: String, actual: T,
                           matcher: Matcher<in T>) {
            MatcherAssert.assertThat(reason, actual, matcher)
        }
    }
}
/**
 * Fails a test with no message.
 */
