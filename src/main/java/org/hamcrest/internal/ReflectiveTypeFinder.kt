/**
 * The TypeSafe classes, and their descendants, need a mechanism to find out what type has been used as a parameter
 * for the concrete matcher. Unfortunately, this type is lost during type erasure so we need to use reflection
 * to get it back, by picking out the type of a known parameter to a known method.
 * The catch is that, with bridging methods, this type is only visible in the class that actually implements
 * the expected method, so the ReflectiveTypeFinder needs to be applied to that class or a subtype.

 * For example, the abstract `TypeSafeDiagnosingMatcher&lt;T&gt;` defines an abstract method
 * protected abstract boolean matchesSafely(T item, Description mismatchDescription);
 * By default it uses `new ReflectiveTypeFinder("matchesSafely", 2, 0); ` to find the
 * parameterised type. If we create a `TypeSafeDiagnosingMatcher&lt;String&gt;`, the type
 * finder will return `String.class`.

 * A `FeatureMatcher` is an abstract subclass of `TypeSafeDiagnosingMatcher`.
 * Although it has a templated implementation of `matchesSafely(&lt;T&gt;, Decription);`, the
 * actualy run-time signature of this is `matchesSafely(Object, Description);`. Instead,
 * we must find the type by reflecting on the concrete implementation of
 * protected abstract U featureValueOf(T actual);
 * a method which is declared in `FeatureMatcher`.

 * In short, use this to extract a type from a method in the leaf class of a templated class hierarchy.

 * @author Steve Freeman
 * *
 * @author Nat Pryce
 */
package org.hamcrest.internal

import java.lang.reflect.Method

class ReflectiveTypeFinder(private val methodName: String, private val expectedNumberOfParameters: Int, private val typedParameter: Int) {

    fun findExpectedType(fromClass: Class<*>): Class<*> {
        var c = fromClass
        while (c != Any::class.java) {
            for (method in c.declaredMethods) {
                if (canObtainExpectedTypeFrom(method)) {
                    return expectedTypeFrom(method)
                }
            }
            c = c.superclass
        }
        throw Error("Cannot determine correct type for $methodName() method.")
    }

    /**
     * @param method The method to examine.
     * *
     * @return true if this method references the relevant type
     */
    protected fun canObtainExpectedTypeFrom(method: Method): Boolean {
        return method.name == methodName
                && method.parameterTypes.size == expectedNumberOfParameters
                && !method.isSynthetic
    }


    /**
     * @param method The method from which to extract
     * *
     * @return The type we're looking for
     */
    protected fun expectedTypeFrom(method: Method): Class<*> {
        return method.parameterTypes[typedParameter]
    }
}