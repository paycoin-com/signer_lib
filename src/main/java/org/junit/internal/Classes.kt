package org.junit.internal

import java.lang.Thread.currentThread

/**
 * Miscellaneous functions dealing with classes.
 */
object Classes {
    /**
     * Returns Class.forName for `className` using the current thread's class loader.

     * @param className Name of the class.
     * *
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    fun getClass(className: String): Class<*> {
        return Class.forName(className, true, currentThread().contextClassLoader)
    }
}
