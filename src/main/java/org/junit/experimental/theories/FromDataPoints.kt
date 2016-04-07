package org.junit.experimental.theories

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.junit.experimental.theories.internal.SpecificDataPointsSupplier

/**
 * Annotating a parameter of a [ &amp;#064;Theory][org.junit.experimental.theories.Theory] method with `&#064;FromDataPoints` will limit the
 * datapoints considered as potential values for that parameter to just the
 * [DataPoints][org.junit.experimental.theories.DataPoints] with the given
 * name. DataPoint names can be given as the value parameter of the
 * &#064;DataPoints annotation.
 *
 *
 * DataPoints without names will not be considered as values for any parameters
 * annotated with &#064;FromDataPoints.
 *
 * &#064;DataPoints
 * public static String[] unnamed = new String[] { ... };

 * &#064;DataPoints("regexes")
 * public static String[] regexStrings = new String[] { ... };

 * &#064;DataPoints({"forMatching", "alphanumeric"})
 * public static String[] testStrings = new String[] { ... };

 * &#064;Theory
 * public void stringTheory(String param) {
 * // This will be called with every value in 'regexStrings',
 * // 'testStrings' and 'unnamed'.
 * }

 * &#064;Theory
 * public void regexTheory(&#064;FromDataPoints("regexes") String regex,
 * &#064;FromDataPoints("forMatching") String value) {
 * // This will be called with only the values in 'regexStrings' as
 * // regex, only the values in 'testStrings' as value, and none
 * // of the values in 'unnamed'.
 * }
 *

 * @see org.junit.experimental.theories.Theory

 * @see org.junit.experimental.theories.DataPoint

 * @see org.junit.experimental.theories.DataPoints
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@ParametersSuppliedBy(SpecificDataPointsSupplier::class)
annotation class FromDataPoints(val value: String)
