package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * If you allocate external resources in a [org.junit.Before] method you need to release them
 * after the test runs. Annotating a `public void` method
 * with `&#064;After` causes that method to be run after the [org.junit.Test] method. All `&#064;After`
 * methods are guaranteed to run even if a [org.junit.Before] or [org.junit.Test] method throws an
 * exception. The `&#064;After` methods declared in superclasses will be run after those of the current
 * class, unless they are overridden in the current class.
 *
 *
 * Here is a simple example:
 *
 * public class Example {
 * File output;
 * &#064;Before public void createOutputFile() {
 * output= new File(...);
 * }
 * &#064;Test public void something() {
 * ...
 * }
 * &#064;After public void deleteOutputFile() {
 * output.delete();
 * }
 * }
 *

 * @see org.junit.Before

 * @see org.junit.Test

 * @since 4.0
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class After

