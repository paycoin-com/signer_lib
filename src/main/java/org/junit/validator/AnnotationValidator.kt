package org.junit.validator

import org.junit.runners.model.FrameworkField
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.TestClass

import java.util.Collections.emptyList

/**
 * Validates annotations on classes and methods. To be validated,
 * an annotation should be annotated with [ValidateWith]

 * Instances of this class are shared by multiple test runners, so they should
 * be immutable and thread-safe.

 * @since 4.12
 */
abstract class AnnotationValidator {

    /**
     * Validates annotation on the given class.

     * @param testClass that is being validated
     * *
     * @return A list of exceptions. Default behavior is to return an empty list.
     * *
     * *
     * @since 4.12
     */
    fun validateAnnotatedClass(testClass: TestClass): List<Exception> {
        return NO_VALIDATION_ERRORS
    }

    /**
     * Validates annotation on the given field.

     * @param field that is being validated
     * *
     * @return A list of exceptions. Default behavior is to return an empty list.
     * *
     * *
     * @since 4.12
     */
    fun validateAnnotatedField(field: FrameworkField): List<Exception> {
        return NO_VALIDATION_ERRORS

    }

    /**
     * Validates annotation on the given method.

     * @param method that is being validated
     * *
     * @return A list of exceptions. Default behavior is to return an empty list.
     * *
     * *
     * @since 4.12
     */
    open fun validateAnnotatedMethod(method: FrameworkMethod): List<Exception> {
        return NO_VALIDATION_ERRORS
    }

    companion object {

        private val NO_VALIDATION_ERRORS = emptyList<Exception>()
    }
}
