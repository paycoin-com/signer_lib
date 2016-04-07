/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher


/**
 * Is the value the same object as another value?
 */
class IsSame<T>(private val `object`: T) : BaseMatcher<T>() {

    override fun matches(arg: Any): Boolean {
        return arg === `object`
    }

    override fun describeTo(description: Description) {
        description.appendText("sameInstance(").appendValue(`object`).appendText(")")
    }

    companion object {

        /**
         * Creates a matcher that matches only when the examined object is the same instance as
         * the specified target object.

         * @param target
         * *     the target instance against which others should be assessed
         */
        @Factory
        fun <T> sameInstance(target: T): Matcher<T> {
            return IsSame(target)
        }

        /**
         * Creates a matcher that matches only when the examined object is the same instance as
         * the specified target object.

         * @param target
         * *     the target instance against which others should be assessed
         */
        @Factory
        fun <T> theInstance(target: T): Matcher<T> {
            return IsSame(target)
        }
    }
}
