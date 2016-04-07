package org.junit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.junit.runners.MethodSorters

/**
 * This class allows the user to choose the order of execution of the methods within a test class.

 *
 * The default order of execution of JUnit tests within a class is deterministic but not predictable.
 * The order of execution is not guaranteed for Java 7 (and some previous versions), and can even change
 * from run to run, so the order of execution was changed to be deterministic (in JUnit 4.11)

 *
 * It is recommended that test methods be written so that they are independent of the order that they are executed.
 * However, there may be a number of dependent tests either through error or by design.
 * This class allows the user to specify the order of execution of test methods.

 *
 * For possibilities, see [MethodSorters]

 * Here is an example:

 *
 * &#064;FixMethodOrder(MethodSorters.NAME_ASCENDING)
 * public class MyTest {
 * }
 *

 * @see org.junit.runners.MethodSorters

 * @since 4.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class FixMethodOrder(
        /**
         * Optionally specify `value` to have the methods executed in a particular order
         */
        val value: MethodSorters = MethodSorters.DEFAULT)
