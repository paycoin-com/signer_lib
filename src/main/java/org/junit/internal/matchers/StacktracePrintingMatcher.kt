package org.junit.internal.matchers

import java.io.PrintWriter
import java.io.StringWriter

import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

/**
 * A matcher that delegates to throwableMatcher and in addition appends the
 * stacktrace of the actual Throwable in case of a mismatch.
 */
class StacktracePrintingMatcher<T : Throwable>(private val throwableMatcher: Matcher<T>) : org.hamcrest.TypeSafeMatcher<T>() {

    override fun describeTo(description: Description) {
        throwableMatcher.describeTo(description)
    }

    override fun matchesSafely(item: T): Boolean {
        return throwableMatcher.matches(item)
    }

    override fun describeMismatchSafely(item: T, description: Description) {
        throwableMatcher.describeMismatch(item, description)
        description.appendText("\nStacktrace was: ")
        description.appendText(readStacktrace(item))
    }

    private fun readStacktrace(throwable: Throwable): String {
        val stringWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }

    companion object {

        @Factory
        fun <T : Throwable> isThrowable(
                throwableMatcher: Matcher<T>): Matcher<T> {
            return StacktracePrintingMatcher(throwableMatcher)
        }

        @Factory
        fun <T : Exception> isException(
                exceptionMatcher: Matcher<T>): Matcher<T> {
            return StacktracePrintingMatcher(exceptionMatcher)
        }
    }
}
