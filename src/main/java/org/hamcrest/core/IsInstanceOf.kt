/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.Description
import org.hamcrest.DiagnosingMatcher
import org.hamcrest.Factory
import org.hamcrest.Matcher


/**
 * Tests whether the value is an instance of a class.
 * Classes of basic types will be converted to the relevant "Object" classes
 */
class IsInstanceOf
/**
 * Creates a new instance of IsInstanceOf

 * @param expectedClass The predicate evaluates to true for instances of this class
 * *                 or one of its subclasses.
 */
(private val expectedClass: Class<*>) : DiagnosingMatcher<Any>() {
    private val matchableClass: Class<*>

    init {
        this.matchableClass = matchableClass(expectedClass)
    }

    override fun matches(item: Any?, mismatch: Description): Boolean {
        if (null == item) {
            mismatch.appendText("null")
            return false
        }

        if (!matchableClass.isInstance(item)) {
            mismatch.appendValue(item).appendText(" is a " + item.javaClass.name)
            return false
        }

        return true
    }

    override fun describeTo(description: Description) {
        description.appendText("an instance of ").appendText(expectedClass.name)
    }

    companion object {

        private fun matchableClass(expectedClass: Class<*>): Class<*> {
            if (Boolean.TYPE == expectedClass) return Boolean::class.java
            if (java.lang.Byte.TYPE == expectedClass) return Byte::class.java
            if (Character.TYPE == expectedClass) return Char::class.java
            if (java.lang.Double.TYPE == expectedClass) return Double::class.java
            if (java.lang.Float.TYPE == expectedClass) return Float::class.java
            if (Integer.TYPE == expectedClass) return Int::class.java
            if (java.lang.Long.TYPE == expectedClass) return Long::class.java
            if (java.lang.Short.TYPE == expectedClass) return Short::class.java
            return expectedClass
        }

        /**
         * Creates a matcher that matches when the examined object is an instance of the specified `type`,
         * as determined by calling the [java.lang.Class.isInstance] method on that type, passing the
         * the examined object.

         *
         * The created matcher assumes no relationship between specified type and the examined object.
         *
         *
         * For example:
         * assertThat(new Canoe(), instanceOf(Paddlable.class));

         */
        @SuppressWarnings("unchecked")
        @Factory
        fun <T> instanceOf(type: Class<*>): Matcher<T> {
            return IsInstanceOf(type) as Matcher<T>
        }

        /**
         * Creates a matcher that matches when the examined object is an instance of the specified `type`,
         * as determined by calling the [java.lang.Class.isInstance] method on that type, passing the
         * the examined object.

         *
         * The created matcher forces a relationship between specified type and the examined object, and should be
         * used when it is necessary to make generics conform, for example in the JMock clause
         * `with(any(Thing.class))`
         *
         *
         * For example:
         * assertThat(new Canoe(), instanceOf(Canoe.class));

         */
        @SuppressWarnings("unchecked")
        @Factory
        fun <T> any(type: Class<T>): Matcher<T> {
            return IsInstanceOf(type) as Matcher<T>
        }
    }

}
