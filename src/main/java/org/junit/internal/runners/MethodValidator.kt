package org.junit.internal.runners

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runners.BlockJUnit4ClassRunner


@Deprecated("")
@Deprecated("Included for backwards compatibility with JUnit 4.4. Will be\n              removed in the next major release. Please use\n              {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.")
class MethodValidator(private val testClass: TestClass) {

    private val errors = ArrayList<Throwable>()

    fun validateInstanceMethods() {
        validateTestMethods(After::class.java, false)
        validateTestMethods(Before::class.java, false)
        validateTestMethods(Test::class.java, false)

        val methods = testClass.getAnnotatedMethods(Test::class.java)
        if (methods.size == 0) {
            errors.add(Exception("No runnable methods"))
        }
    }

    fun validateStaticMethods() {
        validateTestMethods(BeforeClass::class.java, true)
        validateTestMethods(AfterClass::class.java, true)
    }

    fun validateMethodsForDefaultRunner(): List<Throwable> {
        validateNoArgConstructor()
        validateStaticMethods()
        validateInstanceMethods()
        return errors
    }

    @Throws(InitializationError::class)
    fun assertValid() {
        if (!errors.isEmpty()) {
            throw InitializationError(errors)
        }
    }

    fun validateNoArgConstructor() {
        try {
            testClass.constructor
        } catch (e: Exception) {
            errors.add(Exception("Test class should have public zero-argument constructor", e))
        }

    }

    private fun validateTestMethods(annotation: Class<out Annotation>,
                                    isStatic: Boolean) {
        val methods = testClass.getAnnotatedMethods(annotation)

        for (each in methods) {
            if (Modifier.isStatic(each.modifiers) != isStatic) {
                val state = if (isStatic) "should" else "should not"
                errors.add(Exception("Method " + each.name + "() "
                        + state + " be static"))
            }
            if (!Modifier.isPublic(each.declaringClass.modifiers)) {
                errors.add(Exception("Class " + each.declaringClass.name
                        + " should be public"))
            }
            if (!Modifier.isPublic(each.modifiers)) {
                errors.add(Exception("Method " + each.name
                        + " should be public"))
            }
            if (each.returnType != Void.TYPE) {
                errors.add(Exception("Method " + each.name
                        + " should be void"))
            }
            if (each.parameterTypes.size != 0) {
                errors.add(Exception("Method " + each.name
                        + " should have no parameters"))
            }
        }
    }
}
