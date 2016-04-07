package org.junit.rules

import java.util.ArrayList

import org.junit.AssumptionViolatedException
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import org.junit.runners.model.Statement

/**
 * TestWatcher is a base class for Rules that take note of the testing
 * action, without modifying it. For example, this class will keep a log of each
 * passing and failing test:

 *
 * public static class WatchmanTest {
 * private static String watchedLog;

 * &#064;Rule
 * public TestWatcher watchman= new TestWatcher() {
 * &#064;Override
 * protected void failed(Throwable e, Description description) {
 * watchedLog+= description + &quot;\n&quot;;
 * }

 * &#064;Override
 * protected void succeeded(Description description) {
 * watchedLog+= description + &quot; &quot; + &quot;success!\n&quot;;
 * }
 * };

 * &#064;Test
 * public void fails() {
 * fail();
 * }

 * &#064;Test
 * public void succeeds() {
 * }
 * }
 *

 * @since 4.9
 */
abstract class TestWatcher : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val errors = ArrayList<Throwable>()

                startingQuietly(description, errors)
                try {
                    base.evaluate()
                    succeededQuietly(description, errors)
                } catch (@SuppressWarnings("deprecation") e: org.junit.internal.AssumptionViolatedException) {
                    errors.add(e)
                    skippedQuietly(e, description, errors)
                } catch (e: Throwable) {
                    errors.add(e)
                    failedQuietly(e, description, errors)
                } finally {
                    finishedQuietly(description, errors)
                }

                MultipleFailureException.assertEmpty(errors)
            }
        }
    }

    private fun succeededQuietly(description: Description,
                                 errors: MutableList<Throwable>) {
        try {
            succeeded(description)
        } catch (e: Throwable) {
            errors.add(e)
        }

    }

    private fun failedQuietly(e: Throwable, description: Description,
                              errors: MutableList<Throwable>) {
        try {
            failed(e, description)
        } catch (e1: Throwable) {
            errors.add(e1)
        }

    }

    @SuppressWarnings("deprecation")
    private fun skippedQuietly(
            e: org.junit.internal.AssumptionViolatedException, description: Description,
            errors: MutableList<Throwable>) {
        try {
            if (e is AssumptionViolatedException) {
                skipped(e, description)
            } else {
                skipped(e, description)
            }
        } catch (e1: Throwable) {
            errors.add(e1)
        }

    }

    private fun startingQuietly(description: Description,
                                errors: MutableList<Throwable>) {
        try {
            starting(description)
        } catch (e: Throwable) {
            errors.add(e)
        }

    }

    private fun finishedQuietly(description: Description,
                                errors: MutableList<Throwable>) {
        try {
            finished(description)
        } catch (e: Throwable) {
            errors.add(e)
        }

    }

    /**
     * Invoked when a test succeeds
     */
    protected open fun succeeded(description: Description) {
    }

    /**
     * Invoked when a test fails
     */
    protected open fun failed(e: Throwable, description: Description) {
    }

    /**
     * Invoked when a test is skipped due to a failed assumption.
     */
    @SuppressWarnings("deprecation")
    protected open fun skipped(e: AssumptionViolatedException, description: Description) {
        // For backwards compatibility with JUnit 4.11 and earlier, call the legacy version
        val asInternalException = e
        skipped(asInternalException, description)
    }

    /**
     * Invoked when a test is skipped due to a failed assumption.

     */
    @Deprecated("")
    @Deprecated("use {@link #skipped(AssumptionViolatedException, Description)}")
    protected fun skipped(
            e: org.junit.internal.AssumptionViolatedException, description: Description) {
    }

    /**
     * Invoked when a test is about to start
     */
    protected open fun starting(description: Description) {
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    protected open fun finished(description: Description) {
    }
}
