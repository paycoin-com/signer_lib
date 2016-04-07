package junit.framework

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * A test case defines the fixture to run multiple tests. To define a test case
 *
 *  1. implement a subclass of `TestCase`
 *  1. define instance variables that store the state of the fixture
 *  1. initialize the fixture state by overriding [.setUp]
 *  1. clean-up after a test by overriding [.tearDown].
 *
 * Each test runs in its own fixture so there
 * can be no side effects among test runs.
 * Here is an example:
 *
 * public class MathTest extends TestCase {
 * protected double fValue1;
 * protected double fValue2;

 * protected void setUp() {
 * fValue1= 2.0;
 * fValue2= 3.0;
 * }
 * }
 *

 * For each test implement a method which interacts
 * with the fixture. Verify the expected results with assertions specified
 * by calling [junit.framework.Assert.assertTrue] with a boolean.
 *
 * public void testAdd() {
 * double result= fValue1 + fValue2;
 * assertTrue(result == 5.0);
 * }
 *

 * Once the methods are defined you can run them. The framework supports
 * both a static type safe and more dynamic way to run a test.
 * In the static way you override the runTest method and define the method to
 * be invoked. A convenient way to do so is with an anonymous inner class.
 *
 * TestCase test= new MathTest("add") {
 * public void runTest() {
 * testAdd();
 * }
 * };
 * test.run();
 *
 * The dynamic way uses reflection to implement [.runTest]. It dynamically finds
 * and invokes a method.
 * In this case the name of the test case has to correspond to the test method
 * to be run.
 *
 * TestCase test= new MathTest("testAdd");
 * test.run();
 *

 * The tests to be run can be collected into a TestSuite. JUnit provides
 * different *test runners* which can run a test suite and collect the results.
 * A test runner either expects a static method `suite` as the entry
 * point to get a test to run or it will extract the suite automatically.
 *
 * public static Test suite() {
 * suite.addTest(new MathTest("testAdd"));
 * suite.addTest(new MathTest("testDivideByZero"));
 * return suite;
 * }
 *

 * @see TestResult

 * @see TestSuite
 */
abstract class TestCase : Assert, Test {
    /**
     * the name of the test case
     */
    /**
     * Gets the name of a TestCase

     * @return the name of the TestCase
     */
    /**
     * Sets the name of a TestCase

     * @param name the name to set
     */
    var name: String? = null

    /**
     * No-arg constructor to enable serialization. This method
     * is not intended to be used by mere mortals without calling setName().
     */
    constructor() {
        name = null
    }

    /**
     * Constructs a test case with the given name.
     */
    constructor(name: String) {
        this.name = name
    }

    /**
     * Counts the number of test cases executed by run(TestResult result).
     */
    override fun countTestCases(): Int {
        return 1
    }

    /**
     * Creates a default TestResult object

     * @see TestResult
     */
    protected fun createResult(): TestResult {
        return TestResult()
    }

    /**
     * A convenience method to run this test, collecting the results with a
     * default TestResult object.

     * @see TestResult
     */
    fun run(): TestResult {
        val result = createResult()
        run(result)
        return result
    }

    /**
     * Runs the test case and collects the results in TestResult.
     */
    override fun run(result: TestResult) {
        result.run(this)
    }

    /**
     * Runs the bare test sequence.

     * @throws Throwable if any exception is thrown
     */
    @Throws(Throwable::class)
    fun runBare() {
        var exception: Throwable? = null
        setUp()
        try {
            runTest()
        } catch (running: Throwable) {
            exception = running
        } finally {
            try {
                tearDown()
            } catch (tearingDown: Throwable) {
                if (exception == null) exception = tearingDown
            }

        }
        if (exception != null) throw exception
    }

    /**
     * Override to run the test and assert its state.

     * @throws Throwable if any exception is thrown
     */
    @Throws(Throwable::class)
    protected open fun runTest() {
        assertNotNull("TestCase.fName cannot be null", name) // Some VMs crash when calling getMethod(null,null);
        var runMethod: Method? = null
        try {
            // use getMethod to get all public inherited
            // methods. getDeclaredMethods returns all
            // methods of this class but excludes the
            // inherited ones.
            runMethod = javaClass.getMethod(name, *null as Array<Class<Any>>)
        } catch (e: NoSuchMethodException) {
            fail("Method \"$name\" not found")
        }

        if (!Modifier.isPublic(runMethod!!.modifiers)) {
            fail("Method \"$name\" should be public")
        }

        try {
            runMethod.invoke(this)
        } catch (e: InvocationTargetException) {
            e.fillInStackTrace()
            throw e.targetException
        } catch (e: IllegalAccessException) {
            e.fillInStackTrace()
            throw e
        }

    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @Throws(Exception::class)
    protected open fun setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    @Throws(Exception::class)
    protected fun tearDown() {
    }

    /**
     * Returns a string representation of the test case
     */
    override fun toString(): String {
        return name + "(" + javaClass.name + ")"
    }

    companion object {

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertTrue(message: String?, condition: Boolean) {
            Assert.assertTrue(message, condition)
        }

        /**
         * Asserts that a condition is true. If it isn't it throws
         * an AssertionFailedError.
         */
        @SuppressWarnings("deprecation")
        override fun assertTrue(condition: Boolean) {
            Assert.assertTrue(condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws
         * an AssertionFailedError with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertFalse(message: String?, condition: Boolean) {
            Assert.assertFalse(message, condition)
        }

        /**
         * Asserts that a condition is false. If it isn't it throws
         * an AssertionFailedError.
         */
        @SuppressWarnings("deprecation")
        override fun assertFalse(condition: Boolean) {
            Assert.assertFalse(condition)
        }

        /**
         * Fails a test with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun fail(message: String?) {
            Assert.fail(message)
        }

        /**
         * Fails a test with no message.
         */
        @SuppressWarnings("deprecation")
        override fun fail() {
            Assert.fail()
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Any?, actual: Any?) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two objects are equal. If they are not
         * an AssertionFailedError is thrown.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Any, actual: Any) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two Strings are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: String?, actual: String?) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two Strings are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: String, actual: String) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two doubles are equal concerning a delta.  If they are not
         * an AssertionFailedError is thrown with the given message.  If the expected
         * value is infinity then the delta value is ignored.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Double, actual: Double, delta: Double) {
            Assert.assertEquals(message, expected, actual, delta)
        }

        /**
         * Asserts that two doubles are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Double, actual: Double, delta: Double) {
            Assert.assertEquals(expected, actual, delta)
        }

        /**
         * Asserts that two floats are equal concerning a positive delta. If they
         * are not an AssertionFailedError is thrown with the given message. If the
         * expected value is infinity then the delta value is ignored.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Float, actual: Float, delta: Float) {
            Assert.assertEquals(message, expected, actual, delta)
        }

        /**
         * Asserts that two floats are equal concerning a delta. If the expected
         * value is infinity then the delta value is ignored.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Float, actual: Float, delta: Float) {
            Assert.assertEquals(expected, actual, delta)
        }

        /**
         * Asserts that two longs are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Long, actual: Long) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two longs are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Long, actual: Long) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two booleans are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Boolean, actual: Boolean) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two booleans are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Boolean, actual: Boolean) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two bytes are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Byte, actual: Byte) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two bytes are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Byte, actual: Byte) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two chars are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Char, actual: Char) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two chars are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Char, actual: Char) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two shorts are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Short, actual: Short) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two shorts are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Short, actual: Short) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that two ints are equal. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(message: String?, expected: Int, actual: Int) {
            Assert.assertEquals(message, expected, actual)
        }

        /**
         * Asserts that two ints are equal.
         */
        @SuppressWarnings("deprecation")
        override fun assertEquals(expected: Int, actual: Int) {
            Assert.assertEquals(expected, actual)
        }

        /**
         * Asserts that an object isn't null.
         */
        @SuppressWarnings("deprecation")
        override fun assertNotNull(`object`: Any) {
            Assert.assertNotNull(`object`)
        }

        /**
         * Asserts that an object isn't null. If it is
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertNotNull(message: String?, `object`: Any?) {
            Assert.assertNotNull(message, `object`)
        }

        /**
         * Asserts that an object is null. If it isn't an [AssertionError] is
         * thrown.
         * Message contains: Expected:  but was: object

         * @param object Object to check or `null`
         */
        @SuppressWarnings("deprecation")
        override fun assertNull(`object`: Any?) {
            Assert.assertNull(`object`)
        }

        /**
         * Asserts that an object is null.  If it is not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertNull(message: String, `object`: Any?) {
            Assert.assertNull(message, `object`)
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * an AssertionFailedError is thrown with the given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertSame(message: String?, expected: Any, actual: Any) {
            Assert.assertSame(message, expected, actual)
        }

        /**
         * Asserts that two objects refer to the same object. If they are not
         * the same an AssertionFailedError is thrown.
         */
        @SuppressWarnings("deprecation")
        override fun assertSame(expected: Any, actual: Any) {
            Assert.assertSame(expected, actual)
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object an AssertionFailedError is thrown with the
         * given message.
         */
        @SuppressWarnings("deprecation")
        override fun assertNotSame(message: String?, expected: Any, actual: Any) {
            Assert.assertNotSame(message, expected, actual)
        }

        /**
         * Asserts that two objects do not refer to the same object. If they do
         * refer to the same object an AssertionFailedError is thrown.
         */
        @SuppressWarnings("deprecation")
        override fun assertNotSame(expected: Any, actual: Any) {
            Assert.assertNotSame(expected, actual)
        }

        @SuppressWarnings("deprecation")
        override fun failSame(message: String?) {
            Assert.failSame(message)
        }

        @SuppressWarnings("deprecation")
        override fun failNotSame(message: String?, expected: Any, actual: Any) {
            Assert.failNotSame(message, expected, actual)
        }

        @SuppressWarnings("deprecation")
        override fun failNotEquals(message: String, expected: Any, actual: Any) {
            Assert.failNotEquals(message, expected, actual)
        }

        @SuppressWarnings("deprecation")
        override fun format(message: String?, expected: Any, actual: Any): String {
            return Assert.format(message, expected, actual)
        }
    }
}
