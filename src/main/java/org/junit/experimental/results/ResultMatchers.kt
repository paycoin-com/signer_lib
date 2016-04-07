package org.junit.experimental.results

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Matchers on a PrintableResult, to enable JUnit self-tests.
 * For example:

 *
 * assertThat(testResult(HasExpectedException.class), isSuccessful());
 *
 */
object ResultMatchers {
    /**
     * Matches if the tests are all successful
     */
    val isSuccessful: Matcher<PrintableResult>
        get() = failureCountIs(0)

    /**
     * Matches if there are `count` failures
     */
    fun failureCountIs(count: Int): Matcher<PrintableResult> {
        return object : TypeSafeMatcher<PrintableResult>() {
            override fun describeTo(description: Description) {
                description.appendText("has $count failures")
            }

            public override fun matchesSafely(item: PrintableResult): Boolean {
                return item.failureCount() == count
            }
        }
    }

    /**
     * Matches if the result has exactly one failure, and it contains `string`
     */
    fun hasSingleFailureContaining(string: String): Matcher<Any> {
        return object : BaseMatcher<Any>() {
            override fun matches(item: Any): Boolean {
                return item.toString().contains(string) && failureCountIs(1).matches(item)
            }

            override fun describeTo(description: Description) {
                description.appendText("has single failure containing " + string)
            }
        }
    }

    /**
     * Matches if the result has one or more failures, and at least one of them
     * contains `string`
     */
    fun hasFailureContaining(string: String): Matcher<PrintableResult> {
        return object : BaseMatcher<PrintableResult>() {
            override fun matches(item: Any): Boolean {
                return item.toString().contains(string)
            }

            override fun describeTo(description: Description) {
                description.appendText("has failure containing " + string)
            }
        }
    }
}
