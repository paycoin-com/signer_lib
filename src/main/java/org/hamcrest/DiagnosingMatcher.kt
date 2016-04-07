package org.hamcrest

/**
 * TODO(ngd): Document.

 * @param
 */
abstract class DiagnosingMatcher<T> : BaseMatcher<T>() {

    override fun matches(item: Any): Boolean {
        return matches(item, Description.NONE)
    }

    override fun describeMismatch(item: Any, mismatchDescription: Description) {
        matches(item, mismatchDescription)
    }

    protected abstract fun matches(item: Any, mismatchDescription: Description): Boolean
}
