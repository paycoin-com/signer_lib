package org.hamcrest

import org.hamcrest.internal.ReflectiveTypeFinder

/**
 * Convenient base class for Matchers that require a non-null value of a specific type.
 * This simply implements the null check, checks the type and then casts.

 * @author Joe Walnes
 * *
 * @author Steve Freeman
 * *
 * @author Nat Pryce
 */
abstract class TypeSafeMatcher<T> : BaseMatcher<T> {

    private val expectedType: Class<*>

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

    /**
     * Subclasses should implement this. The item will already have been checked for
     * the specific type and will never be null.
     */
    protected abstract fun matchesSafely(item: T): Boolean

    /**
     * Subclasses should override this. The item will already have been checked for
     * the specific type and will never be null.
     */
    protected open fun describeMismatchSafely(item: T, mismatchDescription: Description) {
        super.describeMismatch(item, mismatchDescription)
    }

    /**
     * Methods made final to prevent accidental override.
     * If you need to override this, there's no point on extending TypeSafeMatcher.
     * Instead, extend the [BaseMatcher].
     */
    @SuppressWarnings("unchecked")
    override fun matches(item: Any?): Boolean {
        return item != null
                && expectedType.isInstance(item)
                && matchesSafely(item as T?)
    }

    @SuppressWarnings("unchecked")
    override fun describeMismatch(item: Any?, description: Description) {
        if (item == null) {
            super.describeMismatch(item, description)
        } else if (!expectedType.isInstance(item)) {
            description.appendText("was a ").appendText(item.javaClass.name).appendText(" (").appendValue(item).appendText(")")
        } else {
            describeMismatchSafely(item as T?, description)
        }
    }

    companion object {
        private val TYPE_FINDER = ReflectiveTypeFinder("matchesSafely", 1, 0)
    }
}
/**
 * The default constructor for simple sub types
 */
