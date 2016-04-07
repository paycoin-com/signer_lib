package org.junit.internal.runners.model

import java.lang.reflect.InvocationTargetException

/**
 * When invoked, throws the exception from the reflected method, rather than
 * wrapping it in an InvocationTargetException.
 */
abstract class ReflectiveCallable {
    @Throws(Throwable::class)
    fun run(): Any {
        try {
            return runReflectiveCall()
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }

    }

    @Throws(Throwable::class)
    protected abstract fun runReflectiveCall(): Any
}