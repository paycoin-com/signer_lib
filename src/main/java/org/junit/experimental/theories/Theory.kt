package org.junit.experimental.theories

import java.lang.annotation.ElementType.METHOD

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Marks test methods that should be read as theories by the [Theories][org.junit.experimental.theories.Theories] runner.

 * @see org.junit.experimental.theories.Theories
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Theory(val nullsAccepted: Boolean = true)