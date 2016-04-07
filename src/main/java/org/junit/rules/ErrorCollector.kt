package org.junit.rules

import org.junit.Assert.assertThat

import java.util.ArrayList
import java.util.concurrent.Callable

import org.hamcrest.Matcher
import org.junit.runners.model.MultipleFailureException

/**
 * The ErrorCollector rule allows execution of a test to continue after the
 * first problem is found (for example, to collect _all_ the incorrect rows in a
 * table, and report them all at once):

 *
 * public static class UsesErrorCollectorTwice {
 * &#064;Rule
 * public ErrorCollector collector= new ErrorCollector();

 * &#064;Test
 * public void example() {
 * collector.addError(new Throwable(&quot;first thing went wrong&quot;));
 * collector.addError(new Throwable(&quot;second thing went wrong&quot;));
 * collector.checkThat(getResult(), not(containsString(&quot;ERROR!&quot;)));
 * // all lines will run, and then a combined failure logged at the end.
 * }
 * }
 *

 * @since 4.7
 */
class ErrorCollector : Verifier() {
    private val errors = ArrayList<Throwable>()

    @Throws(Throwable::class)
    override fun verify() {
        MultipleFailureException.assertEmpty(errors)
    }

    /**
     * Adds a Throwable to the table.  Execution continues, but the test will fail at the end.
     */
    fun addError(error: Throwable) {
        errors.add(error)
    }

    /**
     * Adds a failure to the table if `matcher` does not match `value`.
     * Execution continues, but the test will fail at the end if the match fails.
     */
    fun <T> checkThat(value: T, matcher: Matcher<T>) {
        checkThat("", value, matcher)
    }

    /**
     * Adds a failure with the given `reason`
     * to the table if `matcher` does not match `value`.
     * Execution continues, but the test will fail at the end if the match fails.
     */
    fun <T> checkThat(reason: String, value: T, matcher: Matcher<T>) {
        checkSucceeds(Callable<kotlin.Any> {
            assertThat(reason, value, matcher)
            value
        })
    }

    /**
     * Adds to the table the exception, if any, thrown from `callable`.
     * Execution continues, but the test will fail at the end if
     * `callable` threw an exception.
     */
    fun <T> checkSucceeds(callable: Callable<T>): T? {
        try {
            return callable.call()
        } catch (e: Throwable) {
            addError(e)
            return null
        }

    }
}
