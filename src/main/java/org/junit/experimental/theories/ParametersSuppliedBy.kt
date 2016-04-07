package org.junit.experimental.theories

import java.lang.annotation.ElementType.ANNOTATION_TYPE
import java.lang.annotation.ElementType.PARAMETER

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * Annotating a [Theory][org.junit.experimental.theories.Theory] method
 * parameter with &#064;ParametersSuppliedBy causes it to be supplied with
 * values from the named
 * [ParameterSupplier][org.junit.experimental.theories.ParameterSupplier]
 * when run as a theory by the [ Theories][org.junit.experimental.theories.Theories] runner.

 * In addition, annotations themselves can be annotated with
 * &#064;ParametersSuppliedBy, and then used similarly. ParameterSuppliedBy
 * annotations on parameters are detected by searching up this heirarchy such
 * that these act as syntactic sugar, making:

 *
 * &#064;ParametersSuppliedBy(Supplier.class)
 * public &#064;interface SpecialParameter { }

 * &#064;Theory
 * public void theoryMethod(&#064;SpecialParameter String param) {
 * ...
 * }
 *

 * equivalent to:

 *
 * &#064;Theory
 * public void theoryMethod(&#064;ParametersSuppliedBy(Supplier.class) String param) {
 * ...
 * }
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.VALUE_PARAMETER)
annotation class ParametersSuppliedBy(val value: KClass<out ParameterSupplier>)
