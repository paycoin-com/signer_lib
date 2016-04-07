package org.junit.runners.model

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type

import org.junit.internal.runners.model.ReflectiveCallable

/**
 * Represents a method on a test class to be invoked at the appropriate point in
 * test execution. These methods are usually marked with an annotation (such as
 * `@Test`, `@Before`, `@After`, `@BeforeClass`,
 * `@AfterClass`, etc.)

 * @since 4.5
 */
class FrameworkMethod
/**
 * Returns a new `FrameworkMethod` for `method`
 */
(
        /**
         * Returns the underlying Java method
         */
        val method: Method?) : FrameworkMember<FrameworkMethod>() {

    init {
        if (method == null) {
            throw NullPointerException(
                    "FrameworkMethod cannot be created without an underlying method.")
        }
    }

    /**
     * Returns the result of invoking this method on `target` with
     * parameters `params`. [InvocationTargetException]s thrown are
     * unwrapped, and their causes rethrown.
     */
    @Throws(Throwable::class)
    fun invokeExplosively(target: Any, vararg params: Any): Any {
        return object : ReflectiveCallable() {
            @Throws(Throwable::class)
            override fun runReflectiveCall(): Any {
                return method.invoke(target, *params)
            }
        }.run()
    }

    /**
     * Returns the method's name
     */
    override val name: String
        get() = method.getName()

    /**
     * Adds to `errors` if this method:
     *
     *  * is not public, or
     *  * takes parameters, or
     *  * returns something other than void, or
     *  * is static (given `isStatic is false`), or
     *  * is not static (given `isStatic is true`).
     *
     */
    fun validatePublicVoidNoArg(isStatic: Boolean, errors: MutableList<Throwable>) {
        validatePublicVoid(isStatic, errors)
        if (method.getParameterTypes().size != 0) {
            errors.add(Exception("Method " + method.getName() + " should have no parameters"))
        }
    }


    /**
     * Adds to `errors` if this method:
     *
     *  * is not public, or
     *  * returns something other than void, or
     *  * is static (given `isStatic is false`), or
     *  * is not static (given `isStatic is true`).
     *
     */
    fun validatePublicVoid(isStatic: Boolean, errors: MutableList<Throwable>) {
        if (isStatic != isStatic) {
            val state = if (isStatic) "should" else "should not"
            errors.add(Exception("Method " + method.getName() + "() " + state + " be static"))
        }
        if (!isPublic) {
            errors.add(Exception("Method " + method.getName() + "() should be public"))
        }
        if (method.getReturnType() != Void.TYPE) {
            errors.add(Exception("Method " + method.getName() + "() should be void"))
        }
    }

    protected override val modifiers: Int
        get() = method.getModifiers()

    /**
     * Returns the return type of the method
     */
    val returnType: Class<*>
        get() = method.getReturnType()

    /**
     * Returns the return type of the method
     */
    override val type: Class<*>
        get() = returnType

    /**
     * Returns the class where the method is actually declared
     */
    override val declaringClass: Class<*>
        get() = method.getDeclaringClass()

    fun validateNoTypeParametersOnArgs(errors: MutableList<Throwable>) {
        NoGenericTypeParametersValidator(method).validate(errors)
    }

    public override fun isShadowedBy(other: FrameworkMethod): Boolean {
        if (other.name != name) {
            return false
        }
        if (other.parameterTypes.size != parameterTypes.size) {
            return false
        }
        for (i in 0..other.parameterTypes.size - 1) {
            if (other.parameterTypes[i] != parameterTypes[i]) {
                return false
            }
        }
        return true
    }

    override fun equals(obj: Any?): Boolean {
        if (!FrameworkMethod::class.java.isInstance(obj)) {
            return false
        }
        return (obj as FrameworkMethod).method == method
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }

    /**
     * Returns true if this is a no-arg method that returns a value assignable
     * to `type`

     */
    @Deprecated("")
    @Deprecated("This is used only by the Theories runner, and does not\n                  use all the generic type info that it ought to. It will be replaced\n                  with a forthcoming ParameterSignature#canAcceptResultOf(FrameworkMethod)\n                  once Theories moves to junit-contrib.")
    fun producesType(type: Type): Boolean {
        return parameterTypes.size == 0 && type is Class<*>
                && type.isAssignableFrom(method.getReturnType())
    }

    private val parameterTypes: Array<Class<*>>
        get() = method.getParameterTypes()

    /**
     * Returns the annotations on this method
     */
    override val annotations: Array<Annotation>
        get() = method.getAnnotations()

    /**
     * Returns the annotation of type `annotationType` on this method, if
     * one exists.
     */
    override fun <T : Annotation> getAnnotation(annotationType: Class<T>): T {
        return method.getAnnotation(annotationType)
    }

    override fun toString(): String {
        return method.toString()
    }
}
