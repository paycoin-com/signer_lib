package org.junit.experimental.categories

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import org.junit.validator.ValidateWith
import kotlin.reflect.KClass

/**
 * Marks a test class or test method as belonging to one or more categories of tests.
 * The value is an array of arbitrary classes.

 * This annotation is only interpreted by the Categories runner (at present).

 * For example:
 *
 * public interface FastTests {}
 * public interface SlowTests {}

 * public static class A {
 * &#064;Test
 * public void a() {
 * fail();
 * }

 * &#064;Category(SlowTests.class)
 * &#064;Test
 * public void b() {
 * }
 * }

 * &#064;Category({SlowTests.class, FastTests.class})
 * public static class B {
 * &#064;Test
 * public void c() {

 * }
 * }
 *

 * For more usage, see code example on [Categories].
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ValidateWith(CategoryValidator::class)
annotation class Category(vararg val value: KClass<*>)