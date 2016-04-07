package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Sometimes you want to temporarily disable a test or a group of tests. Methods annotated with
 * [org.junit.Test] that are also annotated with `&#064;Ignore` will not be executed as tests.
 * Also, you can annotate a class containing test methods with `&#064;Ignore` and none of the containing
 * tests will be executed. Native JUnit 4 test runners should report the number of ignored tests along with the
 * number of tests that ran and the number of tests that failed.

 *
 * For example:
 *
 * &#064;Ignore &#064;Test public void something() { ...
 *
 * &#064;Ignore takes an optional default parameter if you want to record why a test is being ignored:
 *
 * &#064;Ignore("not ready yet") &#064;Test public void something() { ...
 *
 * &#064;Ignore can also be applied to the test class:
 *
 * &#064;Ignore public class IgnoreMe {
 * &#064;Test public void test1() { ... }
 * &#064;Test public void test2() { ... }
 * }
 *

 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Ignore(
        /**
         * The optional reason why the test is ignored.
         */
        val value: String = "")
