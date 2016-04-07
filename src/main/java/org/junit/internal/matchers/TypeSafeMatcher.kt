package org.junit.internal.matchers

import java.lang.reflect.Method

import org.hamcrest.BaseMatcher
import org.junit.internal.MethodSorter

/**
 * Convenient base class for Matchers that require a non-null value of a specific type.
 * This simply implements the null check, checks the type and then casts.

 * @author Joe Walnes
 * *
 */
@Deprecated("")
@Deprecated("Please use {@link org.hamcrest.TypeSafeMatcher}.")
abstract class TypeSafeMatcher<T> : BaseMatcher<T> {

    private var expectedType: Class<*>? = null

    /**
     * Subclasses should implement this. The item will already have been checked for
     * the specific type and will never be null.
     */
    abstract fun matchesSafely(item: T): Boolean

    protected constructor() {
        expectedType = findExpectedType(javaClass)
    }

    private fun findExpectedType(fromClass: Class<*>): Class<*> {
        var c = fromClass
        while (c != Any::class.java) {
            for (method in MethodSorter.getDeclaredMethods(c)) {
                if (isMatchesSafelyMethod(method)) {
                    return method.parameterTypes[0]
                }
            }
            c = c.superclass
        }

        throw Error("Cannot determine correct type for matchesSafely() method.")
    }

    private fun isMatchesSafelyMethod(method: Method): Boolean {
        return method.name == "matchesSafely"
                && method.parameterTypes.size == 1
                && !method.isSynthetic
    }

    protected constructor(expectedType: Class<T>) {
        this.expectedType = expectedType
    }

    /**
     * Method made final to prevent accidental override.
     * If you need to override this, there's no point on extending TypeSafeMatcher.
     * Instead, extend the [BaseMatcher].
     */
    @SuppressWarnings("unchecked")
    override fun matches(item: Any): Boolean {
        return item != null
                && expectedType!!.isInstance(item)
                && matchesSafely(item as T)
    }
}
