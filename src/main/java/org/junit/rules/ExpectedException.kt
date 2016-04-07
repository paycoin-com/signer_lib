package org.junit.rules

import java.lang.String.format
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.internal.matchers.ThrowableCauseMatcher.hasCause
import org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage

import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.junit.AssumptionViolatedException
import org.junit.runners.model.Statement

/**
 * The `ExpectedException` rule allows you to verify that your code
 * throws a specific exception.

 * Usage

 *  public class SimpleExpectedExceptionTest {
 * &#064;Rule
 * public ExpectedException thrown= ExpectedException.none();

 * &#064;Test
 * public void throwsNothing() {
 * // no exception expected, none thrown: passes.
 * }

 * &#064;Test
 * public void throwsExceptionWithSpecificType() {
 * thrown.expect(NullPointerException.class);
 * throw new NullPointerException();
 * }
 * }

 *
 *
 * You have to add the `ExpectedException` rule to your test.
 * This doesn't affect your existing tests (see `throwsNothing()`).
 * After specifiying the type of the expected exception your test is
 * successful when such an exception is thrown and it fails if a
 * different or no exception is thrown.

 *
 *
 * Instead of specifying the exception's type you can characterize the
 * expected exception based on other criterias, too:

 *
 *  * The exception's message contains a specific text: [.expectMessage]
 *  * The exception's message complies with a Hamcrest matcher: [.expectMessage]
 *  * The exception's cause complies with a Hamcrest matcher: [.expectCause]
 *  * The exception itself complies with a Hamcrest matcher: [.expect]
 *

 *
 *
 * You can combine any of the presented expect-methods. The test is
 * successful if all specifications are met.
 *  &#064;Test
 * public void throwsException() {
 * thrown.expect(NullPointerException.class);
 * thrown.expectMessage(&quot;happened&quot;);
 * throw new NullPointerException(&quot;What happened?&quot;);
 * }

 * AssumptionViolatedExceptions
 *
 *
 * JUnit uses [AssumptionViolatedException]s for indicating that a test
 * provides no useful information. (See [org.junit.Assume] for more
 * information.) You have to call `assume` methods before you set
 * expectations of the `ExpectedException` rule. In this case the rule
 * will not handle consume the exceptions and it can be handled by the
 * framework. E.g. the following test is ignored by JUnit's default runner.

 *  &#064;Test
 * public void ignoredBecauseOfFailedAssumption() {
 * assumeTrue(false); // throws AssumptionViolatedException
 * thrown.expect(NullPointerException.class);
 * }

 * AssertionErrors

 *
 *
 * JUnit uses [AssertionError]s for indicating that a test is failing. You
 * have to call `assert` methods before you set expectations of the
 * `ExpectedException` rule, if they should be handled by the framework.
 * E.g. the following test fails because of the `assertTrue` statement.

 *  &#064;Test
 * public void throwsUnhandled() {
 * assertTrue(false); // throws AssertionError
 * thrown.expect(NullPointerException.class);
 * }

 * Missing Exceptions
 *
 *
 * By default missing exceptions are reported with an error message
 * like "Expected test to throw an instance of foo". You can configure a different
 * message by means of [.reportMissingExceptionWithMessage]. You
 * can use a `%s` placeholder for the description of the expected
 * exception. E.g. "Test doesn't throw %s." will fail with the error message
 * "Test doesn't throw an instance of foo.".

 * @since 4.7
 */
class ExpectedException private constructor() : TestRule {

    private val matcherBuilder = ExpectedExceptionMatcherBuilder()

    private var missingExceptionMessage = "Expected test to throw %s"

    /**
     * This method does nothing. Don't use it.
     */
    @Deprecated("")
    @Deprecated("AssertionErrors are handled by default since JUnit 4.12. Just\n                  like in JUnit &lt;= 4.10.")
    fun handleAssertionErrors(): ExpectedException {
        return this
    }

    /**
     * This method does nothing. Don't use it.
     */
    @Deprecated("")
    @Deprecated("AssumptionViolatedExceptions are handled by default since\n                  JUnit 4.12. Just like in JUnit &lt;= 4.10.")
    fun handleAssumptionViolatedExceptions(): ExpectedException {
        return this
    }

    /**
     * Specifies the failure message for tests that are expected to throw
     * an exception but do not throw any. You can use a `%s` placeholder for
     * the description of the expected exception. E.g. "Test doesn't throw %s."
     * will fail with the error message
     * "Test doesn't throw an instance of foo.".

     * @param message exception detail message
     * *
     * @return the rule itself
     */
    fun reportMissingExceptionWithMessage(message: String): ExpectedException {
        missingExceptionMessage = message
        return this
    }

    override fun apply(base: Statement,
                       description: org.junit.runner.Description): Statement {
        return ExpectedExceptionStatement(base)
    }

    /**
     * Verify that your code throws an exception that is matched by
     * a Hamcrest matcher.
     *  &#064;Test
     * public void throwsExceptionThatCompliesWithMatcher() {
     * NullPointerException e = new NullPointerException();
     * thrown.expect(is(e));
     * throw e;
     * }
     */
    fun expect(matcher: Matcher<*>) {
        matcherBuilder.add(matcher)
    }

    /**
     * Verify that your code throws an exception that is an
     * instance of specific `type`.
     *  &#064;Test
     * public void throwsExceptionWithSpecificType() {
     * thrown.expect(NullPointerException.class);
     * throw new NullPointerException();
     * }
     */
    fun expect(type: Class<out Throwable>) {
        expect(instanceOf(type))
    }

    /**
     * Verify that your code throws an exception whose message contains
     * a specific text.
     *  &#064;Test
     * public void throwsExceptionWhoseMessageContainsSpecificText() {
     * thrown.expectMessage(&quot;happened&quot;);
     * throw new NullPointerException(&quot;What happened?&quot;);
     * }
     */
    fun expectMessage(substring: String) {
        expectMessage(containsString(substring))
    }

    /**
     * Verify that your code throws an exception whose message is matched
     * by a Hamcrest matcher.
     *  &#064;Test
     * public void throwsExceptionWhoseMessageCompliesWithMatcher() {
     * thrown.expectMessage(startsWith(&quot;What&quot;));
     * throw new NullPointerException(&quot;What happened?&quot;);
     * }
     */
    fun expectMessage(matcher: Matcher<String>) {
        expect(hasMessage<Throwable>(matcher))
    }

    /**
     * Verify that your code throws an exception whose cause is matched by
     * a Hamcrest matcher.
     *  &#064;Test
     * public void throwsExceptionWhoseCauseCompliesWithMatcher() {
     * NullPointerException expectedCause = new NullPointerException();
     * thrown.expectCause(is(expectedCause));
     * throw new IllegalArgumentException(&quot;What happened?&quot;, cause);
     * }
     */
    fun expectCause(expectedCause: Matcher<out Throwable>) {
        expect(hasCause<Throwable>(expectedCause))
    }

    private inner class ExpectedExceptionStatement(private val next: Statement) : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            try {
                next.evaluate()
            } catch (e: Throwable) {
                handleException(e)
                return
            }

            if (isAnyExceptionExpected) {
                failDueToMissingException()
            }
        }
    }

    @Throws(Throwable::class)
    private fun handleException(e: Throwable) {
        if (isAnyExceptionExpected) {
            assertThat(e, matcherBuilder.build())
        } else {
            throw e
        }
    }

    private val isAnyExceptionExpected: Boolean
        get() = matcherBuilder.expectsThrowable()

    @Throws(AssertionError::class)
    private fun failDueToMissingException() {
        fail(missingExceptionMessage())
    }

    private fun missingExceptionMessage(): String {
        val expectation = StringDescription.toString(matcherBuilder.build())
        return format(missingExceptionMessage, expectation)
    }

    companion object {
        /**
         * Returns a [rule][TestRule] that expects no exception to
         * be thrown (identical to behavior without this rule).
         */
        fun none(): ExpectedException {
            return ExpectedException()
        }
    }
}
