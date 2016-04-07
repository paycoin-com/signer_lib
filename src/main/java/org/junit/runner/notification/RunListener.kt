package org.junit.runner.notification

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.junit.runner.Description
import org.junit.runner.Result

/**
 * Register an instance of this class with [RunNotifier] to be notified
 * of events that occur during a test run. All of the methods in this class
 * are abstract and have no implementation; override one or more methods to
 * receive events.
 *
 *
 * For example, suppose you have a `Cowbell`
 * class that you want to make a noise whenever a test fails. You could write:
 *
 * public class RingingListener extends RunListener {
 * public void testFailure(Failure failure) {
 * Cowbell.ring();
 * }
 * }
 *
 *
 *
 * To invoke your listener, you need to run your tests through `JUnitCore`.
 *
 * public void main(String... args) {
 * JUnitCore core= new JUnitCore();
 * core.addListener(new RingingListener());
 * core.run(MyTestClass.class);
 * }
 *
 *
 *
 * If a listener throws an exception for a test event, the other listeners will
 * have their [RunListener.testFailure] called with a `Description`
 * of [Description.TEST_MECHANISM] to indicate the failure.
 *
 *
 * By default, JUnit will synchronize calls to your listener. If your listener
 * is thread-safe and you want to allow JUnit to call your listener from
 * multiple threads when tests are run in parallel, you can annotate your
 * test class with [RunListener.ThreadSafe].
 *
 *
 * Listener methods will be called from the same thread as is running
 * the test, unless otherwise indicated by the method Javadoc

 * @see org.junit.runner.JUnitCore

 * @since 4.0
 */
open class RunListener {

    /**
     * Called before any tests have been run. This may be called on an
     * arbitrary thread.

     * @param description describes the tests to be run
     */
    @Throws(Exception::class)
    open fun testRunStarted(description: Description) {
    }

    /**
     * Called when all tests have finished. This may be called on an
     * arbitrary thread.

     * @param result the summary of the test run, including all the tests that failed
     */
    @Throws(Exception::class)
    open fun testRunFinished(result: Result) {
    }

    /**
     * Called when an atomic test is about to be started.

     * @param description the description of the test that is about to be run
     * * (generally a class and method name)
     */
    @Throws(Exception::class)
    open fun testStarted(description: Description) {
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.

     * @param description the description of the test that just ran
     */
    @Throws(Exception::class)
    open fun testFinished(description: Description) {
    }

    /**
     * Called when an atomic test fails, or when a listener throws an exception.

     *
     * In the case of a failure of an atomic test, this method will be called
     * with the same `Description` passed to
     * [.testStarted], from the same thread that called
     * [.testStarted].

     *
     * In the case of a listener throwing an exception, this will be called with
     * a `Description` of [Description.TEST_MECHANISM], and may be called
     * on an arbitrary thread.

     * @param failure describes the test that failed and the exception that was thrown
     */
    @Throws(Exception::class)
    open fun testFailure(failure: Failure) {
    }

    /**
     * Called when an atomic test flags that it assumes a condition that is
     * false

     * @param failure describes the test that failed and the
     * * [org.junit.AssumptionViolatedException] that was thrown
     */
    open fun testAssumptionFailure(failure: Failure) {
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated
     * with [org.junit.Ignore].

     * @param description describes the test that will not be run
     */
    @Throws(Exception::class)
    open fun testIgnored(description: Description) {
    }


    /**
     * Indicates a `RunListener` that can have its methods called
     * concurrently. This implies that the class is thread-safe (i.e. no set of
     * listener calls can put the listener into an invalid state, even if those
     * listener calls are being made by multiple threads without
     * synchronization).

     * @since 4.12
     */
    @Documented
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
    @Retention(RetentionPolicy.RUNTIME)
    annotation class ThreadSafe
}
