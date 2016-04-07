/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest

/**
 * BaseClass for all Matcher implementations.

 * @see Matcher
 */
abstract class BaseMatcher<T> : Matcher<T> {

    /**
     * @see Matcher._dont_implement_Matcher___instead_extend_BaseMatcher_
     */
    @Deprecated("")
    override fun _dont_implement_Matcher___instead_extend_BaseMatcher_() {
        // See Matcher interface for an explanation of this method.
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("was ").appendValue(item)
    }

    override fun toString(): String {
        return StringDescription.toString(this)
    }
}
