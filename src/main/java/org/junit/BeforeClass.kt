package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Sometimes several tests need to share computationally expensive setup
 * (like logging into a database). While this can compromise the independence of
 * tests, sometimes it is a necessary optimization. Annotating a `public static void` no-arg method
 * with `@BeforeClass` causes it to be run once before any of
 * the test methods in the class. The `@BeforeClass` methods of superclasses
 * will be run before those of the current class, unless they are shadowed in the current class.
 *
 *
 * For example:
 *
 * public class Example {
 * &#064;BeforeClass public static void onlyOnce() {
 * ...
 * }
 * &#064;Test public void one() {
 * ...
 * }
 * &#064;Test public void two() {
 * ...
 * }
 * }
 *

 * @see org.junit.AfterClass

 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class BeforeClass
