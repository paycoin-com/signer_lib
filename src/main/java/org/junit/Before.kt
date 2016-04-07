package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * When writing tests, it is common to find that several tests need similar
 * objects created before they can run. Annotating a `public void` method
 * with `&#064;Before` causes that method to be run before the [org.junit.Test] method.
 * The `&#064;Before` methods of superclasses will be run before those of the current class,
 * unless they are overridden in the current class. No other ordering is defined.
 *
 *
 * Here is a simple example:
 *
 * public class Example {
 * List empty;
 * &#064;Before public void initialize() {
 * empty= new ArrayList();
 * }
 * &#064;Test public void size() {
 * ...
 * }
 * &#064;Test public void remove() {
 * ...
 * }
 * }
 *

 * @see org.junit.BeforeClass

 * @see org.junit.After

 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Before

