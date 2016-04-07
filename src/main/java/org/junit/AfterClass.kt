package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * If you allocate expensive external resources in a [org.junit.BeforeClass] method you need to release them
 * after all the tests in the class have run. Annotating a `public static void` method
 * with `&#064;AfterClass` causes that method to be run after all the tests in the class have been run. All `&#064;AfterClass`
 * methods are guaranteed to run even if a [org.junit.BeforeClass] method throws an
 * exception. The `&#064;AfterClass` methods declared in superclasses will be run after those of the current
 * class, unless they are shadowed in the current class.
 *
 *
 * Here is a simple example:
 *
 * public class Example {
 * private static DatabaseConnection database;
 * &#064;BeforeClass public static void login() {
 * database= ...;
 * }
 * &#064;Test public void something() {
 * ...
 * }
 * &#064;Test public void somethingElse() {
 * ...
 * }
 * &#064;AfterClass public static void logout() {
 * database.logout();
 * }
 * }
 *

 * @see org.junit.BeforeClass

 * @see org.junit.Test

 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class AfterClass
