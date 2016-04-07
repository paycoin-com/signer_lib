package org.junit.internal.runners

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Collections

import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.internal.MethodSorter
import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class TestClass(val javaClass: Class<*>) {

    val testMethods: List<Method>
        get() = getAnnotatedMethods(Test::class.java)

    internal val befores: List<Method>
        get() = getAnnotatedMethods(BeforeClass::class.java)

    internal val afters: List<Method>
        get() = getAnnotatedMethods(AfterClass::class.java)

    fun getAnnotatedMethods(annotationClass: Class<out Annotation>): List<Method> {
        val results = ArrayList<Method>()
        for (eachClass in getSuperClasses(javaClass)) {
            val methods = MethodSorter.getDeclaredMethods(eachClass)
            for (eachMethod in methods) {
                val annotation = eachMethod.getAnnotation<out Annotation>(annotationClass)
                if (annotation != null && !isShadowed(eachMethod, results)) {
                    results.add(eachMethod)
                }
            }
        }
        if (runsTopToBottom(annotationClass)) {
            Collections.reverse(results)
        }
        return results
    }

    private fun runsTopToBottom(annotation: Class<out Annotation>): Boolean {
        return annotation == Before::class.java || annotation == BeforeClass::class.java
    }

    private fun isShadowed(method: Method, results: List<Method>): Boolean {
        for (each in results) {
            if (isShadowed(method, each)) {
                return true
            }
        }
        return false
    }

    private fun isShadowed(current: Method, previous: Method): Boolean {
        if (previous.name != current.name) {
            return false
        }
        if (previous.parameterTypes.size != current.parameterTypes.size) {
            return false
        }
        for (i in 0..previous.parameterTypes.size - 1) {
            if (previous.parameterTypes[i] != current.parameterTypes[i]) {
                return false
            }
        }
        return true
    }

    private fun getSuperClasses(testClass: Class<*>): List<Class<*>> {
        val results = ArrayList<Class<*>>()
        var current: Class<*>? = testClass
        while (current != null) {
            results.add(current)
            current = current.superclass
        }
        return results
    }

    val constructor: Constructor<*>
        @Throws(SecurityException::class, NoSuchMethodException::class)
        get() = javaClass.getConstructor()

    val name: String
        get() = javaClass.name

}
