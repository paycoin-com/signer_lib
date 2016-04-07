package org.hamcrest

import java.lang.annotation.ElementType.METHOD
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * Marks a Hamcrest static factory method so tools recognise them.
 * A factory method is an equivalent to a named constructor.

 * @author Joe Walnes
 */
@Retention(RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Factory
