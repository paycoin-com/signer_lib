package org.junit.experimental.theories.suppliers

import java.lang.annotation.ElementType.PARAMETER

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.junit.experimental.theories.ParametersSuppliedBy

/**
 * Annotating a [Theory][org.junit.experimental.theories.Theory] method int
 * parameter with &#064;TestedOn causes it to be supplied with values from the
 * ints array given when run as a theory by the
 * [Theories][org.junit.experimental.theories.Theories] runner. For
 * example, the below method would be called three times by the Theories runner,
 * once with each of the int parameters specified.

 *
 * &#064;Theory
 * public void shouldPassForSomeInts(&#064;TestedOn(ints={1, 2, 3}) int param) {
 * ...
 * }
 *
 */
@ParametersSuppliedBy(TestedOnSupplier::class)
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class TestedOn(val ints: IntArray)
