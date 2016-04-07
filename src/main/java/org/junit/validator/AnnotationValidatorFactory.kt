package org.junit.validator

import java.util.concurrent.ConcurrentHashMap

/**
 * Creates instances of Annotation Validators.

 * @since 4.12
 */
class AnnotationValidatorFactory {

    /**
     * Creates the AnnotationValidator specified by the value in
     * [org.junit.validator.ValidateWith]. Instances are
     * cached.

     * @return An instance of the AnnotationValidator.
     * *
     * *
     * @since 4.12
     */
    fun createAnnotationValidator(validateWithAnnotation: ValidateWith): AnnotationValidator {
        val validator = VALIDATORS_FOR_ANNOTATION_TYPES[validateWithAnnotation]
        if (validator != null) {
            return validator
        }

        val clazz = validateWithAnnotation.value() ?: throw IllegalArgumentException("Can't create validator, value is null in annotation " + validateWithAnnotation.javaClass.name)
        try {
            val annotationValidator = clazz!!.newInstance()
            VALIDATORS_FOR_ANNOTATION_TYPES.putIfAbsent(validateWithAnnotation, annotationValidator)
            return VALIDATORS_FOR_ANNOTATION_TYPES[validateWithAnnotation]
        } catch (e: Exception) {
            throw RuntimeException("Exception received when creating AnnotationValidator class " + clazz!!.getName(), e)
        }

    }

    companion object {
        private val VALIDATORS_FOR_ANNOTATION_TYPES = ConcurrentHashMap<ValidateWith, AnnotationValidator>()
    }

}
