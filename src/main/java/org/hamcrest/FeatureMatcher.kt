package org.hamcrest

import org.hamcrest.internal.ReflectiveTypeFinder

/**
 * Supporting class for matching a feature of an object. Implement `featureValueOf()`
 * in a subclass to pull out the feature to be matched against.

 * @param  The type of the object to be matched
 * *
 * @param  The type of the feature to be matched
 */
abstract class FeatureMatcher<T, U>
/**
 * Constructor
 * @param subMatcher The matcher to apply to the feature
 * *
 * @param featureDescription Descriptive text to use in describeTo
 * *
 * @param featureName Identifying text for mismatch message
 */
(private val subMatcher: Matcher<in U>, private val featureDescription: String, private val featureName: String) : TypeSafeDiagnosingMatcher<T>(FeatureMatcher.TYPE_FINDER) {

    /**
     * Implement this to extract the interesting feature.
     * @param actual the target object
     * *
     * @return the feature to be matched
     */
    protected abstract fun featureValueOf(actual: T): U

    override fun matchesSafely(actual: T, mismatch: Description): Boolean {
        val featureValue = featureValueOf(actual)
        if (!subMatcher.matches(featureValue)) {
            mismatch.appendText(featureName).appendText(" ")
            subMatcher.describeMismatch(featureValue, mismatch)
            return false
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendText(featureDescription).appendText(" ").appendDescriptionOf(subMatcher)
    }

    companion object {
        private val TYPE_FINDER = ReflectiveTypeFinder("featureValueOf", 1, 0)
    }
}
