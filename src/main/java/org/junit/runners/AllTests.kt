package org.junit.runners

import org.junit.internal.runners.SuiteMethod

/**
 * Runner for use with JUnit 3.8.x-style AllTests classes
 * (those that only implement a static `suite()`
 * method). For example:
 *
 * &#064;RunWith(AllTests.class)
 * public class ProductTests {
 * public static junit.framework.Test suite() {
 * ...
 * }
 * }
 *

 * @since 4.0
 */
class AllTests
/**
 * Only called reflectively. Do not use programmatically.
 */
@Throws(Throwable::class)
constructor(klass: Class<*>) : SuiteMethod(klass)
