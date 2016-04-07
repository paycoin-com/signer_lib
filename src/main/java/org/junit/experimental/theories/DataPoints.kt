package org.junit.experimental.theories

import java.lang.annotation.ElementType.FIELD
import java.lang.annotation.ElementType.METHOD

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * Annotating an array or iterable-typed field or method with &#064;DataPoints
 * will cause the values in the array or iterable given to be used as potential
 * parameters for theories in that class when run with the
 * [Theories][org.junit.experimental.theories.Theories] runner.
 *
 *
 * DataPoints will only be considered as potential values for parameters for
 * which their types are assignable. When multiple sets of DataPoints exist with
 * overlapping types more control can be obtained by naming the DataPoints using
 * the value of this annotation, e.g. with
 * `&#064;DataPoints({"dataset1", "dataset2"})`, and then specifying
 * which named set to consider as potential values for each parameter using the
 * [&amp;#064;FromDataPoints][org.junit.experimental.theories.FromDataPoints]
 * annotation.
 *
 *
 * Parameters with no specified source (i.e. without &#064;FromDataPoints or
 * other [ &amp;#064;ParameterSuppliedBy][org.junit.experimental.theories.ParametersSuppliedBy] annotations) will use all DataPoints that are
 * assignable to the parameter type as potential values, including named sets of
 * DataPoints.
 *
 *
 * DataPoints methods whose array types aren't assignable from the target
 * parameter type (and so can't possibly return relevant values) will not be
 * called when generating values for that parameter. Iterable-typed datapoints
 * methods must always be called though, as this information is not available
 * here after generic type erasure, so expensive methods returning iterable
 * datapoints are a bad idea.

 *
 * &#064;DataPoints
 * public static String[] dataPoints = new String[] { ... };

 * &#064;DataPoints
 * public static String[] generatedDataPoints() {
 * return new String[] { ... };
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
annotation class DataPoints(vararg val value: String = arrayOf(), val ignoredExceptions: Array<KClass<out Throwable>> = arrayOf())
