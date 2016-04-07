/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest


object MatcherAssert {
    fun <T> assertThat(actual: T, matcher: Matcher<in T>) {
        assertThat("", actual, matcher)
    }

    fun <T> assertThat(reason: String, actual: T, matcher: Matcher<in T>) {
        if (!matcher.matches(actual)) {
            val description = StringDescription()
            description.appendText(reason).appendText("\nExpected: ").appendDescriptionOf(matcher).appendText("\n     but: ")
            matcher.describeMismatch(actual, description)

            throw AssertionError(description.toString())
        }
    }

    fun assertThat(reason: String, assertion: Boolean) {
        if (!assertion) {
            throw AssertionError(reason)
        }
    }
}
