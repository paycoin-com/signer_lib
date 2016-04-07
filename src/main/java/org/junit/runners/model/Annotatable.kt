package org.junit.runners.model

/**
 * A model element that may have annotations.

 * @since 4.12
 */
interface Annotatable {
    /**
     * Returns the model elements' annotations.
     */
    val annotations: Array<Annotation>

    /**
     * Returns the annotation on the model element of the given type, or @code{null}
     */
    fun <T : Annotation> getAnnotation(annotationType: Class<T>): T
}
