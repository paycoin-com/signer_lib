package org.junit.validator

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * Allows for an [AnnotationValidator] to be attached to an annotation.

 *
 * When attached to an annotation, the validator will be instantiated and invoked
 * by the [org.junit.runners.ParentRunner].

 * @since 4.12
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
annotation class ValidateWith(val value: KClass<out AnnotationValidator>)
