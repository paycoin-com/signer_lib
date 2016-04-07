package org.junit

import java.util.Arrays.asList
import org.hamcrest.CoreMatchers.everyItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue

import org.hamcrest.Matcher

/**
 * A set of methods useful for stating assumptions about the conditions in which a test is meaningful.
 * A failed assumption does not mean the code is broken, but that the test provides no useful information. Assume
 * basically means "don't run this test if these conditions don't apply". The default JUnit runner skips tests with
 * failing assumptions. Custom runners may behave differently.
 *
 *
 * A good example of using assumptions is in [Theories](https://github.com/junit-team/junit/wiki/Theories) where they are needed to exclude certain datapoints that aren't suitable or allowed for a certain test case.
 *
 * Failed assumptions are usually not logged, because there may be many tests that don't apply to certain
 * configurations.

 *
 *
 * These methods can be used directly: `Assume.assumeTrue(...)`, however, they
 * read better if they are referenced through static import:
 *
 * import static org.junit.Assume.*;
 * ...
 * assumeTrue(...);
 *
 *

 * @see [Theories](https://github.com/junit-team/junit/wiki/Theories)


 * @since 4.4
 */
object Assume {
    /**
     * If called with an expression evaluating to `false`, the test will halt and be ignored.
     */
    fun assumeTrue(b: Boolean) {
        assumeThat(b, `is`(true))
    }

    /**
     * The inverse of [.assumeTrue].
     */
    fun assumeFalse(b: Boolean) {
        assumeTrue(!b)
    }

    /**
     * If called with an expression evaluating to `false`, the test will halt and be ignored.

     * @param b If `false`, the method will attempt to stop the test and ignore it by
     * * throwing [AssumptionViolatedException].
     * *
     * @param message A message to pass to [AssumptionViolatedException].
     */
    fun assumeTrue(message: String, b: Boolean) {
        if (!b) throw AssumptionViolatedException(message)
    }

    /**
     * The inverse of [.assumeTrue].
     */
    fun assumeFalse(message: String, b: Boolean) {
        assumeTrue(message, !b)
    }

    /**
     * If called with one or more null elements in `objects`, the test will halt and be ignored.
     */
    fun assumeNotNull(vararg objects: Any) {
        assumeThat(asList(*objects), everyItem(notNullValue()))
    }

    /**
     * Call to assume that `actual` satisfies the condition specified by `matcher`.
     * If not, the test halts and is ignored.
     * Example:
     * :
     * assumeThat(1, is(1)); // passes
     * foo(); // will execute
     * assumeThat(0, is(1)); // assumption failure! test halts
     * int x = 1 / 0; // will never execute
     *

     * @param  the static type accepted by the matcher (this can flag obvious compile-time problems such as `assumeThat(1, is(&quot;a&quot;))`
     * *
     * @param actual the computed value being compared
     * *
     * @param matcher an expression, built of [Matcher]s, specifying allowed values
     * *
     * @see org.hamcrest.CoreMatchers

     * @see org.junit.matchers.JUnitMatchers
     */
    fun <T> assumeThat(actual: T, matcher: Matcher<T>) {
        if (!matcher.matches(actual)) {
            throw AssumptionViolatedException(actual, matcher)
        }
    }

    /**
     * Call to assume that `actual` satisfies the condition specified by `matcher`.
     * If not, the test halts and is ignored.
     * Example:
     * :
     * assumeThat("alwaysPasses", 1, is(1)); // passes
     * foo(); // will execute
     * assumeThat("alwaysFails", 0, is(1)); // assumption failure! test halts
     * int x = 1 / 0; // will never execute
     *

     * @param  the static type accepted by the matcher (this can flag obvious compile-time problems such as `assumeThat(1, is(&quot;a&quot;))`
     * *
     * @param actual the computed value being compared
     * *
     * @param matcher an expression, built of [Matcher]s, specifying allowed values
     * *
     * @see org.hamcrest.CoreMatchers

     * @see org.junit.matchers.JUnitMatchers
     */
    fun <T> assumeThat(message: String, actual: T, matcher: Matcher<T>) {
        if (!matcher.matches(actual)) {
            throw AssumptionViolatedException(message, actual, matcher)
        }
    }

    /**
     * Use to assume that an operation completes normally.  If `e` is non-null, the test will halt and be ignored.

     * For example:
     *
     * \@Test public void parseDataFile() {
     * DataFile file;
     * try {
     * file = DataFile.open("sampledata.txt");
     * } catch (IOException e) {
     * // stop test and ignore if data can't be opened
     * assumeNoException(e);
     * }
     * // ...
     * }
     *

     * @param e if non-null, the offending exception
     */
    fun assumeNoException(e: Throwable) {
        assumeThat(e, nullValue())
    }

    /**
     * Attempts to halt the test and ignore it if Throwable `e` is
     * not `null`. Similar to [.assumeNoException],
     * but provides an additional message that can explain the details
     * concerning the assumption.

     * @param e if non-null, the offending exception
     * *
     * @param message Additional message to pass to [AssumptionViolatedException].
     * *
     * @see .assumeNoException
     */
    fun assumeNoException(message: String, e: Throwable) {
        assumeThat(message, e, nullValue())
    }
}
