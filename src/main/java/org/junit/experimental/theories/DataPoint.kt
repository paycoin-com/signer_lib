package org.junit.experimental.theories

import java.lang.annotation.ElementType.FIELD
import java.lang.annotation.ElementType.METHOD

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * Annotating an field or method with &#064;DataPoint will cause the field value
 * or the value returned by the method to be used as a potential parameter for
 * theories in that class, when run with the
 * [Theories][org.junit.experimental.theories.Theories] runner.
 *
 *
 * A DataPoint is only considered as a potential value for parameters for
 * which its type is assignable. When multiple `DataPoint`s exist
 * with overlapping types more control can be obtained by naming each DataPoint
 * using the value of this annotation, e.g. with
 * `&#064;DataPoint({"dataset1", "dataset2"})`, and then specifying
 * which named set to consider as potential values for each parameter using the
 * [&amp;#064;FromDataPoints][org.junit.experimental.theories.FromDataPoints]
 * annotation.
 *
 *
 * Parameters with no specified source (i.e. without &#064;FromDataPoints or
 * other [ &amp;#064;ParameterSuppliedBy][org.junit.experimental.theories.ParametersSuppliedBy] annotations) will use all `DataPoint`s that are
 * assignable to the parameter type as potential values, including named sets of
 * `DataPoint`s.

 *
 * &#064;DataPoint
 * public static String dataPoint = "value";

 * &#064;DataPoint("generated")
 * public static String generatedDataPoint() {
 * return "generated value";
 * }

 * &#064;Theory
 * public void theoryMethod(String param) {
 * ...
 * }
 *

 * @see org.junit.experimental.theories.Theories

 * @see org.junit.experimental.theories.Theory

 * @see org.junit.experimental.theories.DataPoint

 * @see org.junit.experimental.theories.FromDataPoints
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class DataPoint(vararg val value: String = arrayOf(), val ignoredExceptions: Array<KClass<out Throwable>> = arrayOf())