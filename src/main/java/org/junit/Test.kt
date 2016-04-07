package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * The `Test` annotation tells JUnit that the `public void` method
 * to which it is attached can be run as a test case. To run the method,
 * JUnit first constructs a fresh instance of the class then invokes the
 * annotated method. Any exceptions thrown by the test will be reported
 * by JUnit as a failure. If no exceptions are thrown, the test is assumed
 * to have succeeded.
 *
 *
 * A simple test looks like this:
 *
 * public class Example {
 * **&#064;Test**
 * public void method() {
 * org.junit.Assert.assertTrue( new ArrayList().isEmpty() );
 * }
 * }
 *
 *
 *
 * The `Test` annotation supports two optional parameters.
 * The first, `expected`, declares that a test method should throw
 * an exception. If it doesn't throw an exception or if it throws a different exception
 * than the one declared, the test fails. For example, the following test succeeds:
 *
 * &#064;Test(**expected=IndexOutOfBoundsException.class**) public void outOfBounds() {
 * new ArrayList&lt;Object&gt;().get(1);
 * }
 *
 * If the exception's message or one of its properties should be verified, the
 * [ExpectedException][org.junit.rules.ExpectedException] rule can be used. Further
 * information about exception testing can be found at the
 * [JUnit Wiki](https://github.com/junit-team/junit/wiki/Exception-testing).
 *
 *
 * The second optional parameter, `timeout`, causes a test to fail if it takes
 * longer than a specified amount of clock time (measured in milliseconds). The following test fails:
 *
 * &#064;Test(**timeout=100**) public void infinity() {
 * while(true);
 * }
 *
 * **Warning**: while `timeout` is useful to catch and terminate
 * infinite loops, it should *not* be considered deterministic. The
 * following test may or may not fail depending on how the operating system
 * schedules threads:
 *
 * &#064;Test(**timeout=100**) public void sleep100() {
 * Thread.sleep(100);
 * }
 *
 * **THREAD SAFETY WARNING:** Test methods with a timeout parameter are run in a thread other than the
 * thread which runs the fixture's @Before and @After methods. This may yield different behavior for
 * code that is not thread safe when compared to the same test method without a timeout parameter.
 * **Consider using the [org.junit.rules.Timeout] rule instead**, which ensures a test method is run on the
 * same thread as the fixture's @Before and @After methods.

 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Test(
        /**
         * Optionally specify `expected`, a Throwable, to cause a test method to succeed if
         * and only if an exception of the specified class is thrown by the method. If the Throwable's
         * message or one of its properties should be verified, the
         * [ExpectedException][org.junit.rules.ExpectedException] rule can be used instead.
         */
        val expected: KClass<out Throwable> = None::class,
        /**
         * Optionally specify `timeout` in milliseconds to cause a test method to fail if it
         * takes longer than that number of milliseconds.
         *
         *
         * **THREAD SAFETY WARNING:** Test methods with a timeout parameter are run in a thread other than the
         * thread which runs the fixture's @Before and @After methods. This may yield different behavior for
         * code that is not thread safe when compared to the same test method without a timeout parameter.
         * **Consider using the [org.junit.rules.Timeout] rule instead**, which ensures a test method is run on the
         * same thread as the fixture's @Before and @After methods.
         *
         */
        val timeout: Long = 0L) {

    /**
     * Default empty exception
     */
    class None private constructor() : Throwable() {
        companion object {
            private val serialVersionUID = 1L
        }
    }
}
