package org.hamcrest

import org.hamcrest.internal.ReflectiveTypeFinder


/**
 * Convenient base class for Matchers that require a non-null value of a specific type
 * and that will report why the received value has been rejected.
 * This implements the null check, checks the type and then casts.
 * To use, implement matchesSafely().

 * @param
 * *
 * @author Neil Dunn
 * *
 * @author Nat Pryce
 * *
 * @author Steve Freeman
 */
abstract class TypeSafeDiagnosingMatcher<T> : BaseMatcher<T> {
    private val expectedType: Class<*>

    /**
     * Subclasses should implement this. The item will already have been checked
     * for the specific type and will never be null.
     */
    protected abstract fun matchesSafely(item: T, mismatchDescription: Description): Boolean

    /**
     * Use this constructor if the subclass that implements `matchesSafely`
     * is *not* the class that binds &lt;T&gt; to a type.
     * @param expectedType The expectedType of the actual value.
     */
    protected constructor(expectedType: Class<*>) {
        this.expectedType = expectedType
    }

    /**
     * Use this constructor if the subclass that implements `matchesSafely`
     * is *not* the class that binds &lt;T&gt; to a type.
     * @param typeFinder A type finder to extract the type
     */
    @JvmOverloads protected constructor(typeFinder: ReflectiveTypeFinder = TYPE_FINDER) {
        this.expectedType = typeFinder.findExpectedType(javaClass)
    }

    @SuppressWarnings("unchecked")
    override fun matches(item: Any?): Boolean {
        return item != null
                && expectedType.isInstance(item)
                && matchesSafely(item as T?, Description.NullDescription())
    }

    @SuppressWarnings("unchecked")
    override fun describeMismatch(item: Any?, mismatchDescription: Description) {
        if (item == null || !expectedType.isInstance(item)) {
            super.describeMismatch(item, mismatchDescription)
        } else {
            matchesSafely(item as T?, mismatchDescription)
        }
    }

    companion object {
        private val TYPE_FINDER = ReflectiveTypeFinder("matchesSafely", 2, 0)
    }
}
/**
 * The default constructor for simple sub types
 */
