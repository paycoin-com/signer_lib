package org.junit.internal.runners

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.Test.None
import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class TestMethod(private val method: Method, private val testClass: TestClass) {

    val isIgnored: Boolean
        get() = method.getAnnotation(Ignore::class.java) != null

    val timeout: Long
        get() {
            val annotation = method.getAnnotation(Test::class.java) ?: return 0
            val timeout = annotation.timeout()
            return timeout
        }

    protected val expectedException: Class<out Throwable>?
        get() {
            val annotation = method.getAnnotation(Test::class.java)
            if (annotation == null || annotation.expected() == None::class.java) {
                return null
            } else {
                return annotation.expected()
            }
        }

    internal fun isUnexpected(exception: Throwable): Boolean {
        return !expectedException!!.isAssignableFrom(exception.javaClass)
    }

    internal fun expectsException(): Boolean {
        return expectedException != null
    }

    internal val befores: List<Method>
        get() = testClass.getAnnotatedMethods(Before::class.java)

    internal val afters: List<Method>
        get() = testClass.getAnnotatedMethods(After::class.java)

    @Throws(IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    operator fun invoke(test: Any) {
        method.invoke(test)
    }

}
