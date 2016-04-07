package org.junit.experimental.categories

import java.util.Arrays.asList
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableSet
import java.util.ArrayList
import java.util.HashSet

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runners.model.FrameworkMethod
import org.junit.validator.AnnotationValidator

/**
 * Validates that there are no errors in the use of the `Category`
 * annotation. If there is, a `Throwable` object will be added to the list
 * of errors.

 * @since 4.12
 */
class CategoryValidator : AnnotationValidator() {

    /**
     * Adds to `errors` a throwable for each problem detected. Looks for
     * `BeforeClass`, `AfterClass`, `Before` and `After`
     * annotations.

     * @param method the method that is being validated
     * *
     * @return A list of exceptions detected
     * *
     * *
     * @since 4.12
     */
    override fun validateAnnotatedMethod(method: FrameworkMethod): List<Exception> {
        val errors = ArrayList<Exception>()
        val annotations = method.annotations
        for (annotation in annotations) {
            for (clazz in INCOMPATIBLE_ANNOTATIONS) {
                if (annotation.annotationType().isAssignableFrom(clazz)) {
                    addErrorMessage(errors, clazz)
                }
            }
        }
        return unmodifiableList(errors)
    }

    private fun addErrorMessage(errors: MutableList<Exception>, clazz: Class<*>) {
        val message = String.format("@%s can not be combined with @Category",
                clazz.simpleName)
        errors.add(Exception(message))
    }

    companion object {

        @SuppressWarnings("unchecked")
        private val INCOMPATIBLE_ANNOTATIONS = unmodifiableSet(HashSet(
                asList(BeforeClass::class.java, AfterClass::class.java, Before::class.java, After::class.java)))
    }
}
