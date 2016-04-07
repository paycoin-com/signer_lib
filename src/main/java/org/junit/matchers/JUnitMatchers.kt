package org.junit.matchers

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.CombinableMatcher.CombinableBothMatcher
import org.hamcrest.core.CombinableMatcher.CombinableEitherMatcher
import org.junit.internal.matchers.StacktracePrintingMatcher

/**
 * Convenience import class: these are useful matchers for use with the assertThat method, but they are
 * not currently included in the basic CoreMatchers class from hamcrest.

 * @since 4.4
 */
object JUnitMatchers {
    /**
     * @return A matcher matching any collection containing element
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#hasItem(Object)} instead.")
    fun <T> hasItem(element: T): Matcher<Iterable<in T>> {
        return CoreMatchers.hasItem(element)
    }

    /**
     * @return A matcher matching any collection containing an element matching elementMatcher
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#hasItem(Matcher)} instead.")
    fun <T> hasItem(elementMatcher: Matcher<in T>): Matcher<Iterable<in T>> {
        return CoreMatchers.hasItem(elementMatcher)
    }

    /**
     * @return A matcher matching any collection containing every element in elements
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#hasItems(Object...)} instead.")
    fun <T> hasItems(vararg elements: T): Matcher<Iterable<T>> {
        return CoreMatchers.hasItems(*elements)
    }

    /**
     * @return A matcher matching any collection containing at least one element that matches
     * *         each matcher in elementMatcher (this may be one element matching all matchers,
     * *         or different elements matching each matcher)
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#hasItems(Matcher...)} instead.")
    fun <T> hasItems(vararg elementMatchers: Matcher<in T>): Matcher<Iterable<T>> {
        return CoreMatchers.hasItems(*elementMatchers)
    }

    /**
     * @return A matcher matching any collection in which every element matches elementMatcher
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#everyItem(Matcher)} instead.")
    fun <T> everyItem(elementMatcher: Matcher<T>): Matcher<Iterable<T>> {
        return CoreMatchers.everyItem(elementMatcher)
    }

    /**
     * @return a matcher matching any string that contains substring
     * *
     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#containsString(String)} instead.")
    fun containsString(substring: java.lang.String): Matcher<java.lang.String> {
        return CoreMatchers.containsString(substring)
    }

    /**
     * This is useful for fluently combining matchers that must both pass.  For example:
     *
     * assertThat(string, both(containsString("a")).and(containsString("b")));
     *

     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#both(Matcher)} instead.")
    fun <T> both(matcher: Matcher<in T>): CombinableBothMatcher<T> {
        return CoreMatchers.both(matcher)
    }

    /**
     * This is useful for fluently combining matchers where either may pass, for example:
     *
     * assertThat(string, either(containsString("a")).or(containsString("b")));
     *

     */
    @Deprecated("")
    @Deprecated("Please use {@link CoreMatchers#either(Matcher)} instead.")
    fun <T> either(matcher: Matcher<in T>): CombinableEitherMatcher<T> {
        return CoreMatchers.either(matcher)
    }

    /**
     * @return A matcher that delegates to throwableMatcher and in addition
     * *         appends the stacktrace of the actual Throwable in case of a mismatch.
     */
    fun <T : Throwable> isThrowable(throwableMatcher: Matcher<T>): Matcher<T> {
        return StacktracePrintingMatcher.isThrowable(throwableMatcher)
    }

    /**
     * @return A matcher that delegates to exceptionMatcher and in addition
     * *         appends the stacktrace of the actual Exception in case of a mismatch.
     */
    fun <T : Exception> isException(exceptionMatcher: Matcher<T>): Matcher<T> {
        return StacktracePrintingMatcher.isException(exceptionMatcher)
    }
}
