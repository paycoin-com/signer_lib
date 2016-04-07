package org.junit.validator

import java.util.Collections.singletonList
import java.util.ArrayList
import java.util.Arrays

import org.junit.runners.model.Annotatable
import org.junit.runners.model.FrameworkField
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.TestClass

/**
 * An `AnnotationsValidator` validates all annotations of a test class,
 * including its annotated fields and methods.

 * @since 4.12
 */
class AnnotationsValidator : TestClassValidator {

    /**
     * Validate all annotations of the specified test class that are be
     * annotated with [ValidateWith].

     * @param testClass
     * *            the [TestClass] that is validated.
     * *
     * @return the errors found by the validator.
     */
    override fun validateTestClass(testClass: TestClass): List<Exception> {
        val validationErrors = ArrayList<Exception>()
        for (validator in VALIDATORS) {
            val additionalErrors = validator.validateTestClass(testClass)
            validationErrors.addAll(additionalErrors)
        }
        return validationErrors
    }

    private abstract class AnnotatableValidator<T : Annotatable> {

        internal abstract fun getAnnotatablesForTestClass(testClass: TestClass): Iterable<T>

        internal abstract fun validateAnnotatable(
                validator: AnnotationValidator, annotatable: T): List<Exception>

        fun validateTestClass(testClass: TestClass): List<Exception> {
            val validationErrors = ArrayList<Exception>()
            for (annotatable in getAnnotatablesForTestClass(testClass)) {
                val additionalErrors = validateAnnotatable(annotatable)
                validationErrors.addAll(additionalErrors)
            }
            return validationErrors
        }

        private fun validateAnnotatable(annotatable: T): List<Exception> {
            val validationErrors = ArrayList<Exception>()
            for (annotation in annotatable.annotations) {
                val annotationType = annotation.annotationType()
                val validateWith = annotationType.getAnnotation(ValidateWith::class.java)
                if (validateWith != null) {
                    val annotationValidator = ANNOTATION_VALIDATOR_FACTORY.createAnnotationValidator(validateWith)
                    val errors = validateAnnotatable(
                            annotationValidator, annotatable)
                    validationErrors.addAll(errors)
                }
            }
            return validationErrors
        }

        companion object {
            private val ANNOTATION_VALIDATOR_FACTORY = AnnotationValidatorFactory()
        }
    }

    private class ClassValidator : AnnotatableValidator<TestClass>() {
        override fun getAnnotatablesForTestClass(testClass: TestClass): Iterable<TestClass> {
            return listOf(testClass)
        }

        override fun validateAnnotatable(
                validator: AnnotationValidator, testClass: TestClass): List<Exception> {
            return validator.validateAnnotatedClass(testClass)
        }
    }

    private class MethodValidator : AnnotatableValidator<FrameworkMethod>() {
        override fun getAnnotatablesForTestClass(
                testClass: TestClass): Iterable<FrameworkMethod> {
            return testClass.annotatedMethods
        }

        override fun validateAnnotatable(
                validator: AnnotationValidator, method: FrameworkMethod): List<Exception> {
            return validator.validateAnnotatedMethod(method)
        }
    }

    private class FieldValidator : AnnotatableValidator<FrameworkField>() {
        override fun getAnnotatablesForTestClass(testClass: TestClass): Iterable<FrameworkField> {
            return testClass.annotatedFields
        }

        override fun validateAnnotatable(
                validator: AnnotationValidator, field: FrameworkField): List<Exception> {
            return validator.validateAnnotatedField(field)
        }
    }

    companion object {
        private val VALIDATORS = Arrays.asList(
                ClassValidator(), MethodValidator(), FieldValidator())
    }
}
