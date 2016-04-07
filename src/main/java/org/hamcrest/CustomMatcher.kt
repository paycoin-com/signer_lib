package org.hamcrest

/**
 * Utility class for writing one off matchers.
 * For example:
 *
 * Matcher&lt;String&gt; aNonEmptyString = new CustomMatcher&lt;String&gt;("a non empty string") {
 * public boolean matches(Object object) {
 * return ((object instanceof String) && !((String) object).isEmpty();
 * }
 * };
 *
 *
 *
 * This class is designed for scenarios where an anonymous inner class
 * matcher makes sense. It should not be used by API designers implementing
 * matchers.

 * @author Neil Dunn
 * *
 * @see CustomTypeSafeMatcher for a type safe variant of this class that you probably
 * want to use.

 * @param  The type of object being matched.
 */
abstract class CustomMatcher<T>(private val fixedDescription: String?) : BaseMatcher<T>() {

    init {
        if (fixedDescription == null) {
            throw IllegalArgumentException("Description should be non null!")
        }
    }

    override fun describeTo(description: Description) {
        description.appendText(fixedDescription)
    }
}
