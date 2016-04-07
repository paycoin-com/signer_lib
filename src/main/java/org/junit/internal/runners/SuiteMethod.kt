package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

import junit.framework.Test

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
 */
open class SuiteMethod @Throws(Throwable::class)
constructor(klass: Class<*>) : JUnit38ClassRunner(SuiteMethod.testFromSuiteMethod(klass)) {
    companion object {

        @Throws(Throwable::class)
        fun testFromSuiteMethod(klass: Class<*>): Test {
            var suiteMethod: Method? = null
            var suite: Test? = null
            try {
                suiteMethod = klass.getMethod("suite")
                if (!Modifier.isStatic(suiteMethod!!.modifiers)) {
                    throw Exception(klass.name + ".suite() must be static")
                }
                suite = suiteMethod.invoke(null) as Test // static method
            } catch (e: InvocationTargetException) {
                throw e.cause
            }

            return suite
        }
    }
}
