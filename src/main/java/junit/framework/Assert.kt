package junit.framework

/**
 * A set of assert methods.  Messages are only displayed when an assert fails.

 */
@Deprecated("")
@Deprecated("Please use {@link org.junit.Assert} instead.")
open class Assert
/**
 * Protect constructor since it is a static only class
 */
protected constructor() {
    companion object {

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError with the given message.
         */
        fun assertTrue(message: String?, condition: Boolean) {
            if (!condition) {
                fail(message)
            }
        }

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError.
         */
        fun assertTrue(condition: Boolean) {
            assertTrue(null, condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws
         * an AssertionFailedError with the given message.
         */
        fun assertFalse(message: String?, condition: Boolean) {
            assertTrue(message, !condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws
         * an AssertionFailedError.
         */
        fun assertFalse(condition: Boolean) {
            assertFalse(null, condition)
        }

        /**
         * Fails a test with the given message.
         */
        @JvmOverloads fun fail(message: String? = null) {
            if (message == null) {
                throw AssertionFailedError()
            }
            throw AssertionFailedError(message)
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Any?, actual: Any?) {
            if (expected == null && actual == null) {
                return
            }
            if (expected != null && expected == actual) {
                return
            }
            failNotEquals(message, expected, actual)
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown.
         */
        fun assertEquals(expected: Any, actual: Any) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two Strings are equal.
         */
        fun assertEquals(message: String?, expected: String?, actual: String?) {
            if (expected == null && actual == null) {
                return
            }
            if (expected != null && expected == actual) {
                return
            }
            val cleanMessage = message ?: ""
            throw ComparisonFailure(cleanMessage, expected, actual)
        }

        /**
         * Asserts that two Strings are equal.
         */
        fun assertEquals(expected: String, actual: String) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two doubles are equal concerning a delta.  If they are not
         * an AssertionFailedError is thrown with the given message.  If the expected
         * value is infinity then the delta value is ignored.
         */
        fun assertEquals(message: String?, expected: Double, actual: Double, delta: Double) {
            if (java.lang.Double.compare(expected, actual) == 0) {
                return
            }
            if (Math.abs(expected - actual) > delta) {
                failNotEquals(message, expected, actual)
            }
        }

        /**
         * Asserts that two doubles are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        fun assertEquals(expected: Double, actual: Double, delta: Double) {
            assertEquals(null, expected, actual, delta)
        }

        /**
         * Asserts that two floats are equal concerning a positive delta. If they
         * are not an AssertionFailedError is thrown with the given message. If the
         * expected value is infinity then the delta value is ignored.
         */
        fun assertEquals(message: String?, expected: Float, actual: Float, delta: Float) {
            if (java.lang.Float.compare(expected, actual) == 0) {
                return
            }
            if (Math.abs(expected - actual) > delta) {
                failNotEquals(message, expected, actual)
            }
        }

        /**
         * Asserts that two floats are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        fun assertEquals(expected: Float, actual: Float, delta: Float) {
            assertEquals(null, expected, actual, delta)
        }

        /**
         * Asserts that two longs are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Long, actual: Long) {
            assertEquals(message, java.lang.Long.valueOf(expected), java.lang.Long.valueOf(actual))
        }

        /**
         * Asserts that two longs are equal.
         */
        fun assertEquals(expected: Long, actual: Long) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two booleans are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Boolean, actual: Boolean) {
            assertEquals(message, java.lang.Boolean.valueOf(expected), java.lang.Boolean.valueOf(actual))
        }

        /**
         * Asserts that two booleans are equal.
         */
        fun assertEquals(expected: Boolean, actual: Boolean) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two bytes are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Byte, actual: Byte) {
            assertEquals(message, java.lang.Byte.valueOf(expected), java.lang.Byte.valueOf(actual))
        }

        /**
         * Asserts that two bytes are equal.
         */
        fun assertEquals(expected: Byte, actual: Byte) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two chars are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Char, actual: Char) {
            assertEquals(message, Character.valueOf(expected), Character.valueOf(actual))
        }

        /**
         * Asserts that two chars are equal.
         */
        fun assertEquals(expected: Char, actual: Char) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two shorts are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Short, actual: Short) {
            assertEquals(message, java.lang.Short.valueOf(expected), java.lang.Short.valueOf(actual))
        }

        /**
         * Asserts that two shorts are equal.
         */
        fun assertEquals(expected: Short, actual: Short) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that two ints are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertEquals(message: String?, expected: Int, actual: Int) {
            assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual))
        }

        /**
         * Asserts that two ints are equal.
         */
        fun assertEquals(expected: Int, actual: Int) {
            assertEquals(null, expected, actual)
        }

        /**
         * Asserts that an object isn't null.
         */
        fun assertNotNull(`object`: Any) {
            assertNotNull(null, `object`)
        }

        /**
         * Asserts that an object isn't null. If it is
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertNotNull(message: String?, `object`: Any?) {
            assertTrue(message, `object` != null)
        }

        /**
         * Asserts that an object is null. If it isn't an [AssertionError] is
         * thrown.
         * Message contains: Expected:  but was: object

         * @param object Object to check or `null`
         */
        fun assertNull(`object`: Any?) {
            if (`object` != null) {
                assertNull("Expected: <null> but was: " + `object`.toString(), `object`)
            }
        }

        /**
         * Asserts that an object is null.  If it is not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertNull(message: String, `object`: Any?) {
            assertTrue(message, `object` == null)
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        fun assertSame(message: String?, expected: Any, actual: Any) {
            if (expected === actual) {
                return
            }
            failNotSame(message, expected, actual)
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * the same an AssertionFailedError is thrown.
         */
        fun assertSame(expected: Any, actual: Any) {
            assertSame(null, expected, actual)
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object an AssertionFailedError is thrown with the
         * given message.
         */
        fun assertNotSame(message: String?, expected: Any, actual: Any) {
            if (expected === actual) {
                failSame(message)
            }
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object an AssertionFailedError is thrown.
         */
        fun assertNotSame(expected: Any, actual: Any) {
            assertNotSame(null, expected, actual)
        }

        fun failSame(message: String?) {
            val formatted = if (message != null) message + " " else ""
            fail(formatted + "expected not same")
        }

        fun failNotSame(message: String?, expected: Any, actual: Any) {
            val formatted = if (message != null) message + " " else ""
            fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">")
        }

        fun failNotEquals(message: String, expected: Any, actual: Any) {
            fail(format(message, expected, actual))
        }

        fun format(message: String?, expected: Any, actual: Any): String {
            var formatted = ""
            if (message != null && message.length > 0) {
                formatted = message + " "
            }
            return formatted + "expected:<" + expected + "> but was:<" + actual + ">"
        }
    }
}
/**
 * Fails a test with no message.
 */
