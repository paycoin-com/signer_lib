package org.hamcrest


/**
 * Utility class for writing one off matchers.
 * For example:
 *
 * Matcher&lt;String&gt; aNonEmptyString = new CustomTypeSafeMatcher&lt;String&gt;("a non empty string") {
 * public boolean matchesSafely(String string) {
 * return !string.isEmpty();
 * }
 * public void describeMismatchSafely(String string, Description mismatchDescription) {
 * mismatchDescription.appendText("was empty");
 * }
 * };
 *
 * This is a variant of [CustomMatcher] that first type checks
 * the argument being matched. By the time [TypeSafeMatcher.matchesSafely] is
 * is called the argument is guaranteed to be non-null and of the correct
 * type.

 * @author Neil Dunn
 * *
 * @param  The type of object being matched
 */
abstract class CustomTypeSafeMatcher<T>(private val fixedDescription: String?) : TypeSafeMatcher<T>() {

    init {
        if (fixedDescription == null) {
            throw IllegalArgumentException("Description must be non null!")
        }
    }

    override fun describeTo(description: Description) {
        description.appendText(fixedDescription)
    }
}
